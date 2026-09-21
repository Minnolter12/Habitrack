package com.minnolter.habitrack.ui.screens.detail

import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import java.time.Instant

/**
 * Breakdown filters for Habit Detail.
 */
val HABIT_DETAIL_TIME_RANGES: List<TimeRange> = listOf(
    TimeRange.TODAY,
    TimeRange.WEEK,
    TimeRange.MONTH,
    TimeRange.YEAR,
    TimeRange.LIFETIME
)

/**
 * Immutable snapshot for Habit Detail stats page.
 */
data class HabitDetailUiState(
    val isLoading: Boolean = true,
    val habitId: Long = 0L,
    val habitName: String = "",
    val habitDescription: String? = null,
    val createdAt: Instant = Instant.EPOCH,
    val accentColorHex: String = ProgressionStage.JUST_STARTED.colorHex,
    val imageUri: String? = null,

    // Lifetime-based hero stats
    val lifetimeMinutes: Long = 0L,
    val stage: ProgressionStage = ProgressionStage.JUST_STARTED,
    val actualProgress: Float = 0f,
    val visualProgress: Float = 0f,

    // Range-filtered breakdown
    val selectedRange: TimeRange = TimeRange.LIFETIME,
    val filteredMinutes: Long = 0L,
    val distributionBuckets: List<DistributionBucket> = emptyList(),

    // Lifetime statistics
    val sessionCount: Int = 0,
    val averageSessionMinutes: Long = 0L,
    val longestSessionMinutes: Long = 0L,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val bestPracticeDayOfWeek: String = "N/A",

    // Session history
    val recentSessions: List<SessionListItem> = emptyList(),

    val dialogState: HabitDetailDialogState = HabitDetailDialogState.None,
    val isDeleted: Boolean = false
) {
    val hasNoSessionsYet: Boolean get() = !isLoading && sessionCount == 0
}

data class DistributionBucket(
    val label: String,
    val minutes: Long
)

data class SessionListItem(
    val id: Long,
    val timestamp: Instant,
    val durationMinutes: Int,
    val note: String?
)

sealed interface HabitDetailDialogState {
    data object None : HabitDetailDialogState
    data object EditHabit : HabitDetailDialogState
    data object DeleteConfirmation : HabitDetailDialogState
    data class ManualSession(val editingSessionId: Long? = null) : HabitDetailDialogState
}
