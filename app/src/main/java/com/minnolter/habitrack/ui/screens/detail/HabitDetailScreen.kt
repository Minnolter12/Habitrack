package com.minnolter.habitrack.ui.screens.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.ui.components.JellyProgressCanvas
import com.minnolter.habitrack.util.formatExactDuration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Stateful entry point: owns the [HabitDetailViewModel] subscription and
 * navigates back automatically once the habit is deleted. All rendering is
 * delegated to the stateless [HabitDetailScreen].
 */
@Composable
fun HabitDetailRoute(
    viewModel: HabitDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    HabitDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onEditHabitClick = viewModel::showEditHabitDialog,
        onDeleteHabitClick = viewModel::showDeleteConfirmationDialog,
        onAddSessionClick = { viewModel.showManualSessionDialog(null) },
        onSessionClick = { sessionId -> viewModel.showManualSessionDialog(sessionId) },
        onSessionDelete = viewModel::deleteSession,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmDeleteHabit = viewModel::archiveOrDeleteHabit,
        onSaveHabit = { name, description -> viewModel.updateHabit(name, description, uiState.imageUri) },
        onSaveSession = { sessionId, minutes, epochMs, note ->
            if (sessionId == null) {
                viewModel.logSession(minutes, epochMs, note)
            } else {
                viewModel.editSession(sessionId, minutes, epochMs, note)
            }
        },
        modifier = modifier
    )
}

/**
 * Pure rendering of [HabitDetailUiState] (Sections 25–28): a top bar with
 * back navigation and an overflow menu, a hero header contrasting the
 * nonlinear jelly fill against the honest linear percentage (Section 20), a
 * distribution chart for the selected breakdown filter, and the session
 * history list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    uiState: HabitDetailUiState,
    onBack: () -> Unit,
    onFilterSelected: (TimeRange) -> Unit,
    onEditHabitClick: () -> Unit,
    onDeleteHabitClick: () -> Unit,
    onAddSessionClick: () -> Unit,
    onSessionClick: (sessionId: Long) -> Unit,
    onSessionDelete: (sessionId: Long) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDeleteHabit: () -> Unit,
    onSaveHabit: (name: String, description: String?) -> Unit,
    onSaveSession: (sessionId: Long?, minutes: Long, timestampEpochMs: Long, note: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var overflowMenuExpanded by remember { mutableStateOf(false) }
    val accentColor = uiState.stage.toColor()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { overflowMenuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = overflowMenuExpanded,
                        onDismissRequest = { overflowMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                overflowMenuExpanded = false
                                onEditHabitClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                overflowMenuExpanded = false
                                onDeleteHabitClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddSessionClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Log Session") }
                )
            }
        }
    ) { contentPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item(key = "hero") {
                    HeroHeader(uiState = uiState, accentColor = accentColor)
                }
                item(key = "filters_and_chart") {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        BreakdownFilterRow(
                            selectedRange = uiState.selectedRange,
                            onRangeSelected = onFilterSelected
                        )
                        FilteredTotalCaption(
                            range = uiState.selectedRange,
                            filteredMinutes = uiState.filteredMinutes
                        )
                        DistributionBarChart(
                            buckets = uiState.distributionBuckets,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .height(140.dp)
                        )
                    }
                }
                item(key = "lifetime_stats") {
                    LifetimeStatsRow(uiState = uiState)
                }
                item(key = "session_history_header") {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                if (uiState.hasNoSessionsYet) {
                    item(key = "no_sessions") {
                        Text(
                            text = "Your first minute starts your journey.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(items = uiState.recentSessions, key = { it.id }) { session ->
                        SessionRow(
                            session = session,
                            onClick = { onSessionClick(session.id) },
                            onDelete = { onSessionDelete(session.id) }
                        )
                    }
                }
            }
        }
    }

    when (val dialog = uiState.dialogState) {
        HabitDetailDialogState.None -> Unit

        HabitDetailDialogState.EditHabit -> EditHabitDialog(
            currentName = uiState.habitName,
            currentDescription = uiState.habitDescription,
            onDismiss = onDismissDialog,
            onSave = onSaveHabit
        )

        HabitDetailDialogState.DeleteConfirmation -> DeleteHabitConfirmationDialog(
            habitName = uiState.habitName,
            onConfirm = onConfirmDeleteHabit,
            onDismiss = onDismissDialog
        )

        is HabitDetailDialogState.ManualSession -> {
            val existingSession = dialog.editingSessionId?.let { id ->
                uiState.recentSessions.find { it.id == id }
            }
            ManualSessionDialog(
                existingSession = existingSession,
                onDismiss = onDismissDialog,
                onSave = { minutes, epochMs, note ->
                    onSaveSession(dialog.editingSessionId, minutes, epochMs, note)
                }
            )
        }
    }
}

@Composable
private fun HeroHeader(uiState: HabitDetailUiState, accentColor: Color) {
    val isMaster = uiState.stage == ProgressionStage.MASTER

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        JellyProgressCanvas(
            visualProgress = uiState.visualProgress,
            stageColor = accentColor,
            isMasterStage = isMaster,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatExactDuration(uiState.lifetimeMinutes),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isMaster) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = uiState.stage.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // The honest, linear counterpart to the jelly's nonlinear visual fill
    // (Section 20): a plain progress bar showing the true percentage toward
    // the 10,000-hour Master threshold, with the exact number spelled out.
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        LinearProgressIndicator(
            progress = { uiState.actualProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.15f)
        )
        Text(
            text = "${(uiState.actualProgress * 100).roundToInt()}% of the way to Master (10,000h)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreakdownFilterRow(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HABIT_DETAIL_TIME_RANGES.forEach { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(range.detailLabel()) }
            )
        }
    }
}

@Composable
private fun FilteredTotalCaption(range: TimeRange, filteredMinutes: Long) {
    Text(
        text = "${range.detailLabel()}: ${formatExactDuration(filteredMinutes)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

/**
 * A minimal, streak-free bar chart (Section 28): every bar is just
 * accumulated minutes for its bucket, nothing implies success/failure or an
 * unbroken chain. Per-bar text labels are only drawn when there are few
 * enough buckets to stay legible (Week: 7, Year: 12); Month's ~30 daily
 * bars and Lifetime's yearly bars rely on the bar heights alone.
 */
@Composable
private fun DistributionBarChart(
    buckets: List<DistributionBucket>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (buckets.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No sessions in this range yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxMinutes = (buckets.maxOfOrNull { it.minutes } ?: 0L).coerceAtLeast(1L)
    val showLabels = buckets.size <= 12

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barCount = buckets.size
            val spacing = size.width * 0.015f
            val barWidth = (size.width - spacing * (barCount - 1)) / barCount

            buckets.forEachIndexed { index, bucket ->
                val barHeightFraction = bucket.minutes.toFloat() / maxMinutes.toFloat()
                val barHeight = size.height * barHeightFraction.coerceIn(0.02f, 1f)
                val left = index * (barWidth + spacing)
                drawRoundRect(
                    color = if (bucket.minutes > 0) accentColor else accentColor.copy(alpha = 0.15f),
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth * 0.3f, barWidth * 0.3f)
                )
            }
        }

        if (showLabels) {
            Row(modifier = Modifier.fillMaxWidth()) {
                buckets.forEach { bucket ->
                    Text(
                        text = bucket.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LifetimeStatsRow(uiState: HabitDetailUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatColumn(label = "Sessions", value = uiState.sessionCount.toString())
        StatColumn(label = "Average", value = formatExactDuration(uiState.averageSessionMinutes))
        StatColumn(label = "Longest", value = formatExactDuration(uiState.longestSessionMinutes))
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionRow(
    session: SessionListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete session",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        ListItem(
            headlineContent = { Text(formatSessionDateLabel(session.timestamp)) },
            supportingContent = session.note?.takeIf { it.isNotBlank() }?.let { note ->
                { Text(text = note) }
            },
            trailingContent = {
                Text(
                    text = formatExactDuration(session.durationMinutes.toLong()),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            modifier = Modifier
                .clickable(onClick = onClick)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
private fun EditHabitDialog(
    currentName: String,
    currentDescription: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?) -> Unit
) {
    var nameField by rememberSaveable { mutableStateOf(currentName) }
    var descriptionField by rememberSaveable { mutableStateOf(currentDescription.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = descriptionField,
                    onValueChange = { descriptionField = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(nameField.trim(), descriptionField.trim().ifBlank { null }) },
                enabled = nameField.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeleteHabitConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $habitName?") },
        text = {
            Text("This permanently deletes $habitName and every logged session. This can't be undone.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/**
 * The combined Section 16/26 manual session dialog: presets aren't offered
 * here (those live in the quick-log sheet) since the point of this dialog is
 * an exact, possibly backdated entry. Only the *date* is user-adjustable —
 * time-of-day defaults to the moment of logging for a new session, or is
 * preserved unchanged when editing an existing one — which is enough to
 * satisfy "log a past or specific session" without a full custom time
 * picker; the date is what actually matters for the day/week/month/year
 * breakdowns this screen shows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualSessionDialog(
    existingSession: SessionListItem?,
    onDismiss: () -> Unit,
    onSave: (minutes: Long, timestampEpochMs: Long, note: String?) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val initialTotalMinutes = existingSession?.durationMinutes ?: 0

    var hoursField by rememberSaveable {
        mutableStateOf(if (initialTotalMinutes > 0) (initialTotalMinutes / 60).toString() else "")
    }
    var minutesField by rememberSaveable {
        mutableStateOf(if (initialTotalMinutes > 0) (initialTotalMinutes % 60).toString() else "")
    }
    var noteField by rememberSaveable { mutableStateOf(existingSession?.note.orEmpty()) }

    val initialEpochDay = remember {
        (existingSession?.timestamp?.atZone(zoneId)?.toLocalDate() ?: LocalDate.now(zoneId)).toEpochDay()
    }
    var selectedEpochDay by rememberSaveable { mutableStateOf(initialEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }

    val totalMinutes = (hoursField.toIntOrNull() ?: 0) * 60 + (minutesField.toIntOrNull() ?: 0)
    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingSession == null) "Log Session" else "Edit Session") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hoursField,
                        onValueChange = { hoursField = it.filter(Char::isDigit) },
                        label = { Text("Hours") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutesField,
                        onValueChange = { minutesField = it.filter(Char::isDigit) },
                        label = { Text("Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteField,
                    onValueChange = { noteField = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val timeOfDay = existingSession?.timestamp?.atZone(zoneId)?.toLocalTime()
                        ?: LocalTime.now(zoneId)
                    val timestamp = selectedDate.atTime(timeOfDay).atZone(zoneId).toInstant()
                    onSave(totalMinutes.toLong(), timestamp.toEpochMilli(), noteField.trim().ifBlank { null })
                },
                enabled = totalMinutes > 0
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker returns UTC-midnight millis for the chosen date
                        // regardless of device zone, so it must be read back in UTC.
                        selectedEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatSessionDateLabel(timestamp: Instant): String {
    val zoneId = ZoneId.systemDefault()
    val sessionDate = timestamp.atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)

    return when (sessionDate) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> sessionDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

private fun TimeRange.detailLabel(): String = when (this) {
    TimeRange.LIFETIME -> "Lifetime"
    TimeRange.TODAY -> "Today"
    TimeRange.WEEK -> "This Week"
    TimeRange.MONTH -> "This Month"
    TimeRange.YEAR -> "This Year"
}

private fun ProgressionStage.toColor(): Color =
    Color(android.graphics.Color.parseColor(colorHex))
