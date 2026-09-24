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

private val PracticeSession.isBaseline: Boolean
    get() = (timestamp == Instant.EPOCH) || (note != null && note.startsWith("Historical baseline", ignoreCase = true))

class HabitDetailViewModel(
    private val habitId: Long,
    private val repository: HabitractRepository,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val selectedRange = MutableStateFlow(TimeRange.LIFETIME)
    private val dialogState = MutableStateFlow<HabitDetailDialogState>(HabitDetailDialogState.None)
    private val isDeleted = MutableStateFlow(false)

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
        val userSessions = sessions.filter { !it.isBaseline }

        val lifetimeMinutes = sessions.sumOf { it.durationMinutes.toLong() }
        val now = Instant.now()

        val filteredMinutes = if (range == TimeRange.LIFETIME) {
            lifetimeMinutes
        } else {
            val bounds = dateRangeProvider.boundsFor(range, now)
            userSessions.filter { session ->
                val epochMillis = session.timestamp.toEpochMilli()
                epochMillis in bounds.startEpochMillis..bounds.endEpochMillis
            }.sumOf { it.durationMinutes.toLong() }
        }

        val userSessionCount = userSessions.size
        val averageSessionMinutes = if (userSessionCount > 0) {
            userSessions.sumOf { it.durationMinutes.toLong() } / userSessionCount
        } else {
            0L
        }

        val longestSessionMinutes = userSessions.maxOfOrNull { it.durationMinutes.toLong() } ?: 0L

        val (currentStreak, longestStreak) = calculateStreaks(userSessions, zoneId)
        val bestDay = calculateBestDayOfWeek(userSessions, zoneId)

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
            distributionBuckets = buildDistributionBuckets(if (range == TimeRange.LIFETIME) sessions else userSessions, range, now),
            sessionCount = userSessionCount,
            averageSessionMinutes = averageSessionMinutes,
            longestSessionMinutes = longestSessionMinutes,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            bestPracticeDayOfWeek = bestDay,
            recentSessions = userSessions
                .sortedByDescending { it.timestamp }
                .take(MAX_RECENT_SESSIONS)
                .map { SessionListItem(it.id, it.timestamp, it.durationMinutes, it.note) },
            dialogState = dialog,
            isDeleted = false
        )
    }

    private fun calculateStreaks(sessions: List<PracticeSession>, zoneId: ZoneId): Pair<Int, Int> {
        if (sessions.isEmpty()) return Pair(0, 0)

        val dates = sessions.map { ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() }
            .distinct()
            .sortedDescending()

        if (dates.isEmpty()) return Pair(0, 0)

        val today = LocalDate.now(zoneId)
        val yesterday = today.minusDays(1)

        var currentStreak = 0
        var checkDate: LocalDate? = if (dates.contains(today)) today else if (dates.contains(yesterday)) yesterday else null

        if (checkDate != null) {
            while (dates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate?.minusDays(1)
            }
        }

        var longestStreak = 0
        var tempStreak = 0
        val sortedAsc = dates.sorted()

        var prevDate: LocalDate? = null
        for (date in sortedAsc) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                tempStreak++
            } else if (date != prevDate) {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) longestStreak = tempStreak
            prevDate = date
        }

        return Pair(currentStreak, longestStreak)
    }

    private fun calculateBestDayOfWeek(sessions: List<PracticeSession>, zoneId: ZoneId): String {
        if (sessions.isEmpty()) return "N/A"
        val dayMinutesMap = sessions.groupBy {
            ZonedDateTime.ofInstant(it.timestamp, zoneId).dayOfWeek
        }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }

        val bestDay = dayMinutesMap.maxByOrNull { it.value }?.key ?: return "N/A"
        return bestDay.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    private fun buildDistributionBuckets(
        sessions: List<PracticeSession>,
        range: TimeRange,
        now: Instant
    ): List<DistributionBucket> {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)

        return when (range) {
            TimeRange.TODAY -> {
                val todayDate = zonedNow.toLocalDate()
                val todaySessions = sessions.filter {
                    ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() == todayDate
                }
                listOf(0, 3, 6, 9, 12, 15, 18, 21).map { hour ->
                    val hourMinutes = todaySessions.filter {
                        val hourOfDay = ZonedDateTime.ofInstant(it.timestamp, zoneId).hour
                        hourOfDay in hour..(hour + 2)
                    }.sumOf { it.durationMinutes.toLong() }
                    val label = when (hour) {
                        0 -> "12a"
                        6 -> "6a"
                        12 -> "12p"
                        18 -> "6p"
                        else -> "${if (hour > 12) hour - 12 else hour}${if (hour >= 12) "p" else "a"}"
                    }
                    DistributionBucket(label = label, minutes = hourMinutes)
                }
            }

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

            TimeRange.LIFETIME -> {
                val startYear = if (sessions.isNotEmpty()) {
                    sessions.minOf { ZonedDateTime.ofInstant(it.timestamp, zoneId).year }
                } else {
                    zonedNow.year
                }
                val endYear = maxOf(zonedNow.year + 3, startYear + 4)
                (startYear..endYear).map { year ->
                    val minutes = sessions.filter {
                        ZonedDateTime.ofInstant(it.timestamp, zoneId).year == year
                    }.sumOf { it.durationMinutes.toLong() }
                    DistributionBucket(label = year.toString(), minutes = minutes)
                }
            }
        }
    }

    private fun List<PracticeSession>.minutesOnLocalDate(
        date: LocalDate,
        zoneId: ZoneId
    ): Long = filter { ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() == date }
        .sumOf { it.durationMinutes.toLong() }

    fun onFilterSelected(range: TimeRange) {
        selectedRange.value = range
    }

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

    fun archiveOrDeleteHabit() {
        val current = habitAggregates.value ?: return
        viewModelScope.launch {
            repository.deleteHabit(current.habit)
            dialogState.value = HabitDetailDialogState.None
            isDeleted.value = true
        }
    }

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
        const val MAX_RECENT_SESSIONS = 200
    }
}

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
