package com.minnolter.habitrack.ui.screens.home

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Home screen: the global dashboard (Sections 9–10) and the
 * habit list (Section 12). All business logic — range resolution, stage
 * derivation, visual progress — lives here; the Composable layer only ever
 * renders [uiState].
 *
 * [dateRangeProvider] isn't called directly by this ViewModel today (range
 * resolution happens inside [HabitractRepository]'s implementation), but is
 * accepted here so a future range-aware, ViewModel-side computation (e.g. a
 * "custom range" filter) doesn't require changing the constructor again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: HabitractRepository,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider()
) : ViewModel() {

    private val selectedTimeRange = MutableStateFlow(TimeRange.LIFETIME)

    private val _isReorderMode = MutableStateFlow(false)

    /**
     * Whether Home is currently showing the Section 24 reorder UI instead of
     * the normal jelly-card list. Owned here (rather than folded into
     * [HomeUiState]) because it's a pure UI-mode flag with no dependency on
     * [HabitractRepository] data — [HomeScreen] observes it directly.
     */
    val isReorderMode: StateFlow<Boolean> = _isReorderMode.asStateFlow()

    val uiState: StateFlow<HomeUiState> =
        combine(repository.observeHabits(), selectedTimeRange) { habits, range -> habits to range }
            .flatMapLatest { (habits, range) -> observeUiState(habits, range) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
                initialValue = HomeUiState(isLoading = true)
            )

    private fun observeUiState(habits: List<Habit>, range: TimeRange): Flow<HomeUiState> {
        if (habits.isEmpty()) {
            return flowOf(
                HomeUiState(
                    isLoading = false,
                    selectedTimeRange = range,
                    dashboardTotalMinutes = 0L,
                    habits = emptyList()
                )
            )
        }

        val habitSummaryFlows = habits.map { habit -> observeHabitSummary(habit, range) }

        return combine(habitSummaryFlows) { summaries -> summaries.toList() }
            .combine(repository.observeTotalMinutes(range)) { summaries, dashboardTotalMinutes ->
                HomeUiState(
                    isLoading = false,
                    selectedTimeRange = range,
                    dashboardTotalMinutes = dashboardTotalMinutes.toLong(),
                    habits = summaries.sortedBy { it.habit.sortOrder }
                )
            }
    }

    private fun observeHabitSummary(habit: Habit, range: TimeRange): Flow<HabitSummary> {
        val lifetimeMinutesFlow = repository.observeMinutes(habit.id, TimeRange.LIFETIME)

        return if (range == TimeRange.LIFETIME) {
            lifetimeMinutesFlow.map { lifetimeMinutes ->
                buildHabitSummary(habit, lifetimeMinutes.toLong(), lifetimeMinutes.toLong())
            }
        } else {
            combine(
                lifetimeMinutesFlow,
                repository.observeMinutes(habit.id, range)
            ) { lifetimeMinutes, rangeMinutes ->
                buildHabitSummary(habit, lifetimeMinutes.toLong(), rangeMinutes.toLong())
            }
        }
    }

    private fun buildHabitSummary(
        habit: Habit,
        lifetimeMinutes: Long,
        selectedRangeMinutes: Long
    ): HabitSummary = HabitSummary(
        habit = habit,
        lifetimeMinutes = lifetimeMinutes,
        selectedRangeMinutes = selectedRangeMinutes,
        stage = ProgressionStage.fromMinutes(lifetimeMinutes),
        visualProgress = ProgressionStage.calculateVisualProgress(lifetimeMinutes)
    )

    /** Called when the user taps a different dashboard filter chip (Section 10). */
    fun onTimeRangeSelected(range: TimeRange) {
        selectedTimeRange.value = range
    }

    /**
     * Persists a full drag-to-reorder result from Reorder mode (Section 24).
     * [habitIdsInOrder] must contain every habit id, in its new top-to-bottom
     * display order.
     */
    fun onHabitsReordered(habitIdsInOrder: List<Long>) {
        viewModelScope.launch {
            repository.reorderHabits(habitIdsInOrder)
        }
    }

    /**
     * Enters Section 24 reorder mode. Reachable from the Home top app bar
     * action (this phase's prompt) and, via a back-navigation callback wired
     * in `HabitractNavHost`, from the "Reorder habits" row in Settings (the
     * spec's Section 24 entry point) — both land here, so there's exactly one
     * reorder mode to reason about regardless of how the user got into it.
     */
    fun enterReorderMode() {
        _isReorderMode.value = true
    }

    /**
     * Leaves reorder mode. [newOrder], when non-null, is the full list of
     * habit ids in their new top-to-bottom order and is persisted via
     * [onHabitsReordered]; passing null (a Cancel) discards whatever the user
     * dragged without touching [Habit.sortOrder] in Room.
     */
    fun exitReorderMode(newOrder: List<Long>?) {
        _isReorderMode.value = false
        if (newOrder != null) {
            onHabitsReordered(newOrder)
        }
    }

    /**
     * Dispatches a quick time log from the Log Practice sheet (Section 16).
     * [minutes] must be a positive duration; the timestamp is always "now" —
     * Habitract does not support backdating a session from the quick-log flow.
     */
    fun logPractice(habitId: Long, minutes: Int) {
        require(minutes > 0) { "Logged duration must be positive, was $minutes minutes." }
        viewModelScope.launch {
            repository.logSession(
                PracticeSession(
                    id = 0L,
                    habitId = habitId,
                    durationMinutes = minutes,
                    timestamp = Instant.now()
                )
            )
        }
    }
}

/**
 * No DI framework is wired up yet (deferred past Phase 1/2), so
 * [HomeViewModel] is provided via a plain [ViewModelProvider.Factory] rather
 * than an injected constructor.
 */
class HomeViewModelFactory(
    private val repository: HabitractRepository,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repository, dateRangeProvider) as T
    }
}
