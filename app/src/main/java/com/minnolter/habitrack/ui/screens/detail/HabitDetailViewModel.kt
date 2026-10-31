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
import java.time.format.DateTimeFormatter
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

    private val selectedRange = MutableStateFlow(TimeRange.MONTH)
    private val selectedRangeOffset = MutableStateFlow(0)
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
        selectedRangeOffset,
        dialogState,
        isDeleted
    ) { aggregates, range, rangeOffset, dialog, deleted ->
        when {
            deleted -> HabitDetailUiState(isLoading = false, habitId = habitId, isDeleted = true)
            aggregates == null -> HabitDetailUiState(isLoading = true, habitId = habitId)
            else -> buildUiState(aggregates, range, rangeOffset, dialog)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = HabitDetailUiState(isLoading = true, habitId = habitId)
    )

    private fun buildUiState(
        aggregates: HabitAggregates,
        range: TimeRange,
        rangeOffset: Int,
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
            val bounds = dateRangeProvider.boundsFor(range, now, rangeOffset)
            userSessions.filter { session ->
                val epochMillis = session.timestamp.toEpochMilli()
                epochMillis in bounds.startEpochMillis..bounds.endEpochMillis
            }.sumOf { it.durationMinutes.toLong() }
        }

        val userSessionCount = userSessions.size
        val activeDaysCount = userSessions.map {
            ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate()
        }.distinct().size

        val totalUserMinutes = userSessions.sumOf { it.durationMinutes.toLong() }
        val averageSessionMinutes = if (activeDaysCount > 0) {
            totalUserMinutes / activeDaysCount
        } else {
            0L
        }

        val longestSessionMinutes = userSessions.maxOfOrNull { it.durationMinutes.toLong() } ?: 0L

        val (currentStreak, longestStreak) = calculateStreaks(userSessions, zoneId)
        val bestDay = calculateBestPracticeDay(userSessions, zoneId)

        val daysInvestedNonstop = lifetimeMinutes / 1440.0f
        val booksEquivalent = lifetimeMinutes / 360.0f
        val moviesEquivalent = lifetimeMinutes / 120.0f
        val peakFocus = calculatePeakFocusWindow(userSessions, zoneId)
        val consistencyScore = calculateConsistencyScore(userSessions, zoneId)

        val daysSinceCreation = maxOf(1L, java.time.Duration.between(habit.createdAt, now).toDays())
        val weeksSinceCreation = maxOf(1.0f, daysSinceCreation / 7.0f)
        val weeklyVelocity = (lifetimeMinutes / 60.0f) / weeksSinceCreation

        val (nextMilestoneHours, hoursRemaining) = calculateNextMilestone(lifetimeMinutes)

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
            selectedRangeOffset = rangeOffset,
            periodLabel = dateRangeProvider.periodLabel(range, now, rangeOffset),
            filteredMinutes = filteredMinutes,
            distributionBuckets = buildDistributionBuckets(if (range == TimeRange.LIFETIME) sessions else userSessions, range, now, rangeOffset),
            sessionCount = userSessionCount,
            averageSessionMinutes = averageSessionMinutes,
            longestSessionMinutes = longestSessionMinutes,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            bestPracticeDayOfWeek = bestDay,
            daysInvestedNonstop = daysInvestedNonstop,
            booksEquivalent = booksEquivalent,
            moviesEquivalent = moviesEquivalent,
            peakFocusWindow = peakFocus,
            weeklyVelocityHours = weeklyVelocity,
            consistencyScorePercent = consistencyScore,
            nextMilestoneHours = nextMilestoneHours,
            hoursToNextMilestone = hoursRemaining,
            recentSessions = userSessions
                .sortedByDescending { it.timestamp }
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

    private fun calculateBestPracticeDay(sessions: List<PracticeSession>, zoneId: ZoneId): String {
        if (sessions.isEmpty()) return "N/A"
        val dateMinutesMap = sessions.groupBy {
            ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }

        val bestDate = dateMinutesMap.maxByOrNull { it.value }?.key ?: return "N/A"
        return bestDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy (EEEE)"))
    }

    private fun calculatePeakFocusWindow(sessions: List<PracticeSession>, zoneId: ZoneId): String {
        if (sessions.isEmpty()) return "N/A"
        val hourCounts = sessions.map { ZonedDateTime.ofInstant(it.timestamp, zoneId).hour }
        val peakHour = hourCounts.groupBy { it }.maxByOrNull { it.value.size }?.key ?: 12
        return when (peakHour) {
            in 5..11 -> "🌅 Morning Dynamo"
            in 12..16 -> "☀️ Afternoon Crusher"
            in 17..21 -> "🌆 Twilight Achiever"
            else -> "🌙 Night Owl Focus"
        }
    }

    private fun calculateConsistencyScore(sessions: List<PracticeSession>, zoneId: ZoneId): Int {
        if (sessions.isEmpty()) return 0
        val today = LocalDate.now(zoneId)
        val thirtyDaysAgo = today.minusDays(30)
        val activeInLast30 = sessions.map { ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() }
            .filter { !it.isBefore(thirtyDaysAgo) }
            .distinct().size
        return ((activeInLast30 / 30.0f) * 100).toInt().coerceIn(0, 100)
    }

    private fun calculateNextMilestone(lifetimeMinutes: Long): Pair<Long, Float> {
        val milestones = listOf(10L, 25L, 50L, 100L, 250L, 500L, 1000L, 2500L, 5000L, 10000L)
        val lifetimeHours = lifetimeMinutes / 60.0f
        val nextMilestone = milestones.firstOrNull { it > lifetimeHours.toLong() } ?: 10000L
        val hoursRemaining = (nextMilestone - lifetimeHours).coerceAtLeast(0f)
        return Pair(nextMilestone, hoursRemaining)
    }

    private fun buildDistributionBuckets(
        sessions: List<PracticeSession>,
        range: TimeRange,
        now: Instant,
        offset: Int = 0
    ): List<DistributionBucket> {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)

        return when (range) {
            TimeRange.TODAY -> {
                val targetDate = zonedNow.toLocalDate().plusDays(offset.toLong())
                val targetSessions = sessions.filter {
                    ZonedDateTime.ofInstant(it.timestamp, zoneId).toLocalDate() == targetDate
                }
                listOf(0, 3, 6, 9, 12, 15, 18, 21).map { hour ->
                    val hourMinutes = targetSessions.filter {
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
                val currentMonday = zonedNow.toLocalDate().minusDays((zonedNow.dayOfWeek.value - 1).toLong())
                val weekStart = currentMonday.plusWeeks(offset.toLong())
                (0..6).map { dayOffset ->
                    val day = weekStart.plusDays(dayOffset.toLong())
                    DistributionBucket(
                        label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        minutes = sessions.minutesOnLocalDate(day, zoneId)
                    )
                }
            }

            TimeRange.MONTH -> {
                val currentMonthStart = zonedNow.toLocalDate().withDayOfMonth(1)
                val monthStart = currentMonthStart.plusMonths(offset.toLong())
                (0 until monthStart.lengthOfMonth()).map { dayOffset ->
                    val day = monthStart.plusDays(dayOffset.toLong())
                    DistributionBucket(
                        label = day.dayOfMonth.toString(),
                        minutes = sessions.minutesOnLocalDate(day, zoneId)
                    )
                }
            }

            TimeRange.YEAR -> {
                val targetYear = zonedNow.year + offset
                (1..12).map { month ->
                    val minutes = sessions.filter { session ->
                        val local = ZonedDateTime.ofInstant(session.timestamp, zoneId)
                        local.year == targetYear && local.monthValue == month
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
        selectedRangeOffset.value = 0
    }

    fun onOffsetSelected(offset: Int) {
        selectedRangeOffset.value = offset.coerceAtMost(0)
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
