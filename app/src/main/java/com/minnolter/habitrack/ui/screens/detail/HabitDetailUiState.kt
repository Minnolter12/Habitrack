package com.minnolter.habitrack.ui.screens.detail

import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import java.time.Instant

/**
 * The breakdown filters offered on the Habit Detail screen. A subset of the
 * full [TimeRange] enum — [TimeRange.TODAY] doesn't produce a meaningful
 * distribution chart bucket set, so it's intentionally left out here even
 * though it remains a valid range elsewhere in the app (the Home dashboard).
 */
val HABIT_DETAIL_TIME_RANGES: List<TimeRange> = listOf(
    TimeRange.WEEK,
    TimeRange.MONTH,
    TimeRange.YEAR,
    TimeRange.LIFETIME
)

/**
 * Immutable snapshot of everything the Habit Detail screen needs to render
 * (Sections 25–28): hero stats, a distribution chart for the selected range,
 * recent session history, and which management dialog (if any) is open.
 *
 * All aggregates are pre-derived by `HabitDetailViewModel` — this state is
 * pure display data, never something the screen queries or computes itself.
 */
data class HabitDetailUiState(
    val isLoading: Boolean = true,
    val habitId: Long = 0L,
    val habitName: String = "",
    val habitDescription: String? = null,
    val createdAt: Instant = Instant.EPOCH,
    val accentColorHex: String = ProgressionStage.JUST_STARTED.colorHex,
    val imageUri: String? = null,

    // Lifetime-based hero stats (Section 20/23 — always lifetime, never range-filtered).
    val lifetimeMinutes: Long = 0L,
    val stage: ProgressionStage = ProgressionStage.JUST_STARTED,
    /** The true, linear percentage toward 10,000h — the honest number (Section 20). */
    val actualProgress: Float = 0f,
    /** The nonlinear, card-friendly fill value — decorative only. */
    val visualProgress: Float = 0f,

    // Range-filtered breakdown (Section 26).
    val selectedRange: TimeRange = TimeRange.WEEK,
    val filteredMinutes: Long = 0L,
    val distributionBuckets: List<DistributionBucket> = emptyList(),

    // Lifetime statistics (Section 26).
    val sessionCount: Int = 0,
    val averageSessionMinutes: Long = 0L,
    val longestSessionMinutes: Long = 0L,

    // Session history (Section 26).
    val recentSessions: List<SessionListItem> = emptyList(),

    val dialogState: HabitDetailDialogState = HabitDetailDialogState.None,

    /** Set once the habit has been deleted, so the screen knows to navigate back. */
    val isDeleted: Boolean = false
) {
    val hasNoSessionsYet: Boolean get() = !isLoading && sessionCount == 0
}

/** One bar of the distribution chart; [label] is already display-ready (e.g. "Mon", "Sep"). */
data class DistributionBucket(
    val label: String,
    val minutes: Long
)

/** A single row in the session history list. */
data class SessionListItem(
    val id: Long,
    val timestamp: Instant,
    val durationMinutes: Int,
    val note: String?
)

/**
 * Which management dialog, if any, is currently presented. [ManualSession]
 * doubles as both "log a new session" ([editingSessionId] null) and "edit an
 * existing one" ([editingSessionId] set), per the spec's combined Add/Edit
 * dialog.
 */
sealed interface HabitDetailDialogState {
    data object None : HabitDetailDialogState
    data object EditHabit : HabitDetailDialogState
    data object DeleteConfirmation : HabitDetailDialogState
    data class ManualSession(val editingSessionId: Long? = null) : HabitDetailDialogState
}
