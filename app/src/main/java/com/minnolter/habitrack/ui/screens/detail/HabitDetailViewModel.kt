package com.minnolter.habitrack.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.PracticeSession
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.util.DateRangeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Habit Detail screen (Sections 25–28) for a single [habitId].
 *
 * Deliberately fetches the habit's *full* session list once via
 * [HabitractRepository.observeSessions] rather than issuing a fresh
 * range-filtered query every time [onFilterSelected] changes: the filtered
 * total and the distribution buckets are both derived from that same list
 * in [buildUiState]/[buildDistributionBuckets], which is simpler and avoids
 * duplicate queries for what's normally a modest number of rows per habit.
 */
class HabitDetailViewModel(
    private val habitId: Long,
    private val repository: HabitractRepository,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val selectedRange = MutableStateFlow(TimeRange.WEEK)
    private val dialogState = MutableStateFlow<HabitDetailDialogState>(HabitDetailDialogState.None)
    private val isDeleted = MutableStateFlow(false)

    /**
     * Kept as its own [StateFlow] (rather than folded straight into
     * [uiState]) so [updateHabit] and [archiveOrDeleteHabit] can read the
     * last-known full [Habit] synchronously via `.value` and `.copy()` it,
     * without needing every one of its fields (e.g. `sortOrder`) duplicated
     * into [HabitDetailUiState] just to round-trip them back out again.
     */
    private val habitAggregates: StateFlow<HabitAggregates?> = combine(
        repository.observeHabit(habitId),
        repository.observeSessions(habitId),
        repository.observeSessionCount(habitId),
        repository.observeLongestSession(habitId)
    ) { habit, sessions, sessionCount, longestSession ->
        habit?.let {
            HabitAggregates(
                habit = it,
                sessions = sessions,
                sessionCount = sessionCount,
                longestSession = longestSession
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<HabitDetailUiState> = combine(
        habitAggregates,
        selectedRange,
        dialogState,
        isDeleted
    ) { aggregates, range, dialog, deleted ->
        when {
            deleted -> HabitDetailUiState(isLoading = false, habitId = habitId, isDeleted = true)
            aggregates == null -> HabitDetailUiState(isLoading = true, habitId = habitId)
            else -> buildUiState(aggregates, range, dialog)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = HabitDetailUiState(isLoading = true, habitId = habitId)
    )

    private fun buildUiState(
        aggregates: HabitAggregates,
        range: TimeRange,
        dialog: HabitDetailDialogState
    ): HabitDetailUiState {
        val habit = aggregates.habit
        val sessions = aggregates.sessions
        val lifetimeMinutes = sessions.sumOf { it.durationMinutes.toLong() }
        val now = Instant.now()

        val filteredMinutes = if (range == TimeRange.LIFETIME) {
            lifetimeMinutes
        } else {
            val bounds = dateRangeProvider.boundsFor(range, now)
            sessions.filter { session ->
                val epochMillis = session.timestamp.toEpochMilli()
                epochMillis in bounds.startEpochMillis..bounds.endEpochMillis
            }.sumOf { it.durationMinutes.toLong() }
        }

        val averageSessionMinutes = if (aggregates.sessionCount > 0) {
            lifetimeMinutes / aggregates.sessionCount
        } else {
            0L
        }

        return HabitDetailUiState(
            isLoading = false,
            habitId = habit.id,
            habitName = habit.name,
            habitDescription = habit.description,
            createdAt = habit.createdAt,
            accentColorHex = habit.colorHex,
            imageUri = habit.imageUri,
            lifetimeMinutes = lifetimeMinutes,
            stage = ProgressionStage.fromMinutes(lifetimeMinutes),
            actualProgress = ProgressionStage.calculateActualProgress(lifetimeMinutes),
            visualProgress = ProgressionStage.calculateVisualProgress(lifetimeMinutes),
            selectedRange = range,
            filteredMinutes = filteredMinutes,
            distributionBuckets = buildDistributionBuckets(sessions, range, now),
            sessionCount = aggregates.sessionCount,
            averageSessionMinutes = averageSessionMinutes,
            longestSessionMinutes = aggregates.longestSession?.durationMinutes?.toLong() ?: 0L,
            recentSessions = sessions
                .sortedByDescending { it.timestamp }
                .take(MAX_RECENT_SESSIONS)
                .map { SessionListItem(it.id, it.timestamp, it.durationMinutes, it.note) },
            dialogState = dialog,
            isDeleted = false
        )
    }

    private fun buildDistributionBuckets(
        sessions: List<PracticeSession>,
        range: TimeRange,
        now: Instant
    ): List<DistributionBucket> {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)

        return when (range) {
            TimeRange.WEEK -> {
                val weekStart = zonedNow.toLocalDate()
                    .minusDays((zonedNow.dayOfWeek.value - 1).toLong())
                (0..6).map { offset ->
                    val day = weekStart.plusDays(offset.toLong())
                    DistributionBucket(
                        label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        minutes = sessions.minutesOnLocalDate(day, zoneId)
                    )
                }
            }

            TimeRange.MONTH -> {
                val monthStart = zonedNow.toLocalDate().withDayOfMonth(1)
                (0 until monthStart.lengthOfMonth()).map { offset ->
                    val day = monthStart.plusDays(offset.toLong())
                    DistributionBucket(
                        label = day.dayOfMonth.toString(),
                        minutes = sessions.minutesOnLocalDate(day, zoneId)
                    )
                }
            }

            TimeRange.YEAR -> {
                (1..12).map { month ->
                    val minutes = sessions.filter { session ->
                        val local = ZonedDateTime.ofInstant(session.timestamp, zoneId)
                        local.year == zonedNow.year && local.monthValue == month
                    }.sumOf { it.durationMinutes.toLong() }
                    DistributionBucket(
                        label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        minutes = minutes
                    )
                }
            }

            TimeRange.LIFETIME, TimeRange.TODAY -> {
                if (sessions.isEmpty()) {
                    emptyList()
                } else {
                    val firstYear = sessions.minOf {
                        ZonedDateTime.ofInstant(it.timestamp, zoneId).year
                    }
                    (firstYear..zonedNow.year).map { year ->
                        val minutes = sessions.filter {
                            ZonedDateTime.ofInstant(it.timestamp, zoneId).year == year
                        }.sumOf { it.durationMinutes.toLong() }
                        DistributionBucket(label = year.toString(), minutes = minutes)
                    }
                }
            }
        }
    }

    private fun List<PracticeSession>.minutesOnLocalDate(
        date: LocalDate,
        zoneId: ZoneId
    ): Long = filter { ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() == date }
        .sumOf { it.durationMinutes.toLong() }

    // ---- Filter ----

    fun onFilterSelected(range: TimeRange) {
        require(range in HABIT_DETAIL_TIME_RANGES) {
            "$range is not a supported Habit Detail breakdown filter."
        }
        selectedRange.value = range
    }

    // ---- Sessions ----

    /** Logs a brand-new session (Section 16/26's manual "log a specific session" entry point). */
    fun logSession(durationMinutes: Long, timestampEpochMs: Long, note: String?) {
        require(durationMinutes > 0) { "Duration must be positive, was $durationMinutes." }
        viewModelScope.launch {
            repository.logSession(
                PracticeSession(
                    id = 0L,
                    habitId = habitId,
                    durationMinutes = durationMinutes.toInt(),
                    timestamp = Instant.ofEpochMilli(timestampEpochMs),
                    note = note?.takeIf { it.isNotBlank() }
                )
            )
            dialogState.value = HabitDetailDialogState.None
        }
    }

    /** Overwrites an existing session (the manual dialog's "edit" mode). */
    fun editSession(sessionId: Long, durationMinutes: Long, timestampEpochMs: Long, note: String?) {
        require(durationMinutes > 0) { "Duration must be positive, was $durationMinutes." }
        viewModelScope.launch {
            repository.updateSession(
                PracticeSession(
                    id = sessionId,
                    habitId = habitId,
                    durationMinutes = durationMinutes.toInt(),
                    timestamp = Instant.ofEpochMilli(timestampEpochMs),
                    note = note?.takeIf { it.isNotBlank() }
                )
            )
            dialogState.value = HabitDetailDialogState.None
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSessionById(sessionId)
        }
    }

    // ---- Habit management ----

    fun updateHabit(name: String, description: String?, imageUri: String?) {
        val current = habitAggregates.value ?: return
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Habit name cannot be blank." }
        viewModelScope.launch {
            repository.updateHabit(
                current.habit.copy(
                    name = trimmedName,
                    description = description?.takeIf { it.isNotBlank() },
                    imageUri = imageUri
                )
            )
            dialogState.value = HabitDetailDialogState.None
        }
    }

    /**
     * Deletes the habit and all of its sessions (cascading FK, Section 32).
     * Named to match the spec's action list; Habitract's data model has no
     * `archived`/hidden flag today, so this is a true delete rather than a
     * soft archive — adding real archiving would need its own schema change
     * and is not part of what this phase's data model supports.
     */
    fun archiveOrDeleteHabit() {
        val current = habitAggregates.value ?: return
        viewModelScope.launch {
            repository.deleteHabit(current.habit)
            dialogState.value = HabitDetailDialogState.None
            isDeleted.value = true
        }
    }

    // ---- Dialogs ----

    fun showEditHabitDialog() {
        dialogState.value = HabitDetailDialogState.EditHabit
    }

    fun showDeleteConfirmationDialog() {
        dialogState.value = HabitDetailDialogState.DeleteConfirmation
    }

    fun showManualSessionDialog(editingSessionId: Long? = null) {
        dialogState.value = HabitDetailDialogState.ManualSession(editingSessionId)
    }

    fun dismissDialog() {
        dialogState.value = HabitDetailDialogState.None
    }

    private data class HabitAggregates(
        val habit: Habit,
        val sessions: List<PracticeSession>,
        val sessionCount: Int,
        val longestSession: PracticeSession?
    )

    private companion object {
        /** Session history is capped for display; the derived lifetime aggregates never are. */
        const val MAX_RECENT_SESSIONS = 200
    }
}

/**
 * No DI framework is wired up yet (see the data-architecture notes), so
 * [HabitDetailViewModel] is provided via a plain [ViewModelProvider.Factory]
 * parameterized by the [habitId] the screen was opened with.
 */
class HabitDetailViewModelFactory(
    private val habitId: Long,
    private val repository: HabitractRepository,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HabitDetailViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return HabitDetailViewModel(habitId, repository, dateRangeProvider) as T
    }
}
