package com.minnolter.habitrack.ui.screens.home

import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange

/**
 * Immutable snapshot of everything the Home screen needs to render: the
 * global dashboard (Section 9–10) and the habit list (Section 12), each
 * habit pre-resolved to its progression stage and card-ready progress value
 * so Composables never touch the repository or perform derivation directly.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedTimeRange: TimeRange = TimeRange.LIFETIME,
    val dashboardTotalMinutes: Long = 0L,
    val habits: List<HabitSummary> = emptyList()
) {
    /** True once loading has finished and there are no habits to show at all (Section 36). */
    val isEmpty: Boolean get() = !isLoading && habits.isEmpty()
}

/**
 * A single habit pre-computed for display: [lifetimeMinutes] always backs
 * the progression stage and jelly fill (Section 17/20 are lifetime-based by
 * design), while [selectedRangeMinutes] reflects whatever [TimeRange] the
 * dashboard filter is currently set to, for any range-aware habit-level UI.
 */
data class HabitSummary(
    val habit: Habit,
    val lifetimeMinutes: Long,
    val selectedRangeMinutes: Long,
    val stage: ProgressionStage,
    val visualProgress: Float
)
