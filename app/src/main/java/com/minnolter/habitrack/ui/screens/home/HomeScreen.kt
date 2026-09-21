package com.minnolter.habitrack.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.ui.components.HabitCard
import com.minnolter.habitrack.ui.components.LogTimeSheet
import com.minnolter.habitrack.ui.screens.home.components.DashboardSummaryHeader
import com.minnolter.habitrack.ui.screens.home.components.ReorderEntry
import com.minnolter.habitrack.ui.screens.home.components.ReorderableHabitList

/**
 * Stateful entry point for the Home screen: owns the [HomeViewModel]
 * subscription, the quick-log sheet's transient visibility, and the
 * in-progress reorder draft (Section 24), and delegates all rendering to the
 * stateless [HomeScreen].
 *
 * The pending reorder result lives here — not in [HomeViewModel] — because
 * it's a discardable UI draft: only a Save actually calls back into the
 * ViewModel (via [HomeViewModel.exitReorderMode]) and touches Room, exactly
 * mirroring how [ReorderableHabitList] itself never talks to a repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onAddHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitLongPress: (habitId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isReorderMode by viewModel.isReorderMode.collectAsStateWithLifecycle()
    var habitPendingLog by remember { mutableStateOf<HabitSummary?>(null) }
    var pendingOrder by remember { mutableStateOf<List<Long>?>(null) }

    HomeScreen(
        uiState = uiState,
        isReorderMode = isReorderMode,
        onTimeRangeSelected = viewModel::onTimeRangeSelected,
        onHabitTap = { summary -> habitPendingLog = summary },
        onHabitLongPress = { summary -> onHabitLongPress(summary.habit.id) },
        onAddHabitClick = onAddHabitClick,
        onSettingsClick = onSettingsClick,
        onReorderModeClick = viewModel::enterReorderMode,
        onReorderDraftChanged = { order -> pendingOrder = order },
        onReorderSaveClick = {
            viewModel.exitReorderMode(pendingOrder)
            pendingOrder = null
        },
        onReorderCancelClick = {
            viewModel.exitReorderMode(null)
            pendingOrder = null
        },
        modifier = modifier
    )

    habitPendingLog?.let { summary ->
        LogTimeSheet(
            habitName = summary.habit.name,
            onDismiss = { habitPendingLog = null },
            onConfirm = { minutes ->
                viewModel.logPractice(summary.habit.id, minutes)
                habitPendingLog = null
            }
        )
    }
}

/**
 * Pure rendering of [HomeUiState] (Sections 8–10, 12, 24, 30, 36): a top app
 * bar with Habitract's wordmark and Settings/Reorder actions, the dashboard,
 * a [LazyColumn] of [HabitCard]s (Section 8 specifically calls for a
 * `LazyColumn`, not a grid), and an Extended FAB for adding a habit — or,
 * while [isReorderMode] is true, the same top bar switched to Cancel/Save
 * actions and the list swapped for [ReorderableHabitList] (Section 24). The
 * FAB and Settings action are both hidden during reorder mode: adding a habit
 * or navigating away mid-drag would either invalidate the in-progress draft
 * or abandon it silently, so both paths are closed off until the user
 * explicitly saves or cancels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isReorderMode: Boolean,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onHabitTap: (HabitSummary) -> Unit,
    onHabitLongPress: (HabitSummary) -> Unit,
    onAddHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReorderModeClick: () -> Unit,
    onReorderDraftChanged: (List<Long>) -> Unit,
    onReorderSaveClick: () -> Unit,
    onReorderCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isReorderMode) "Reorder Habits" else "Habitrack",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (isReorderMode) {
                        IconButton(onClick = onReorderCancelClick) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel reordering")
                        }
                    }
                },
                actions = {
                    if (isReorderMode) {
                        IconButton(onClick = onReorderSaveClick) {
                            Icon(Icons.Filled.Check, contentDescription = "Save new order")
                        }
                    } else {
                        if (!uiState.isEmpty && uiState.habits.size > 1) {
                            IconButton(onClick = onReorderModeClick) {
                                Icon(Icons.Filled.Reorder, contentDescription = "Reorder habits")
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isEmpty && !isReorderMode) {
                ExtendedFloatingActionButton(
                    onClick = onAddHabitClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add Habit") }
                )
            }
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(contentPadding))
            uiState.isEmpty -> EmptyHomeState(
                onAddHabitClick = onAddHabitClick,
                modifier = Modifier.padding(contentPadding)
            )
            isReorderMode -> ReorderableHabitList(
                items = uiState.habits.map { summary ->
                    ReorderEntry(
                        habitId = summary.habit.id,
                        name = summary.habit.name,
                        colorHex = summary.habit.colorHex
                    )
                },
                onOrderChanged = { entries -> onReorderDraftChanged(entries.map { it.habitId }) },
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            )
            else -> HomeContent(
                uiState = uiState,
                onTimeRangeSelected = onTimeRangeSelected,
                onHabitTap = onHabitTap,
                onHabitLongPress = onHabitLongPress,
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onHabitTap: (HabitSummary) -> Unit,
    onHabitLongPress: (HabitSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "dashboard_header") {
            DashboardSummaryHeader(
                selectedRange = uiState.selectedTimeRange,
                totalMinutes = uiState.dashboardTotalMinutes,
                habitCount = uiState.habits.size,
                onRangeSelected = onTimeRangeSelected
            )
        }

        items(items = uiState.habits, key = { it.habit.id }) { summary ->
            HabitCard(
                habit = summary.habit,
                lifetimeMinutes = summary.lifetimeMinutes,
                stage = summary.stage,
                visualProgress = summary.visualProgress,
                onTap = { onHabitTap(summary) },
                onLongPress = { onHabitLongPress(summary) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * The Section 36 empty state, shown only once loading has finished and there
 * genuinely are no habits — never a generic "no data" placeholder.
 */
@Composable
private fun EmptyHomeState(
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your journey starts here.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Choose something you want to invest your time into.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        )
        ExtendedFloatingActionButton(
            onClick = onAddHabitClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Add Habit") }
        )
    }
}
