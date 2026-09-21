package com.minnolter.habitrack.ui.screens.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.ui.components.DeleteHabitConfirmationSheet
import com.minnolter.habitrack.ui.components.JellyProgressCanvas
import com.minnolter.habitrack.util.formatExactDuration
import com.minnolter.habitrack.util.getDynamicHabitColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
    
    val accentColor = getDynamicHabitColor(
        baseStage = uiState.stage,
        lifetimeMinutes = uiState.lifetimeMinutes,
        customColorHex = uiState.accentColorHex
    )

    val cosmicBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF16131D),
            Color(0xFF0F0C15),
            Color(0xFF08060B)
        )
    )

    Scaffold(
        modifier = modifier.background(cosmicBackground),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { overflowMenuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White)
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
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = {
                                overflowMenuExpanded = false
                                onDeleteHabitClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddSessionClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Log Session", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(percent = 50)
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
                CircularProgressIndicator(color = Color(0xFF00E5FF))
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

                item(key = "edge_to_edge_filters") {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        BreakdownFilterRow(
                            selectedRange = uiState.selectedRange,
                            onRangeSelected = onFilterSelected
                        )
                    }
                }

                item(key = "streak_badge") {
                    StreakBadgeCard(
                        currentStreakDays = uiState.currentStreakDays,
                        longestStreakDays = uiState.longestStreakDays
                    )
                }

                item(key = "chart") {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        FilteredTotalCaption(
                            range = uiState.selectedRange,
                            filteredMinutes = uiState.filteredMinutes
                        )
                        DistributionBarChart(
                            buckets = uiState.distributionBuckets,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .height(140.dp)
                        )
                    }
                }

                item(key = "insightful_stats_grid") {
                    InsightfulStatsSection(uiState = uiState)
                }

                item(key = "collapsible_sessions") {
                    CollapsibleRecentSessions(
                        sessions = uiState.recentSessions,
                        onSessionClick = onSessionClick,
                        onSessionDelete = onSessionDelete
                    )
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
            .height(200.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1B1822).copy(alpha = 0.75f))
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatExactDuration(uiState.lifetimeMinutes),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = uiState.stage.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
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
            color = Color.White.copy(alpha = 0.65f),
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
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HABIT_DETAIL_TIME_RANGES) { range ->
            val isSelected = range == selectedRange
            FilterChip(
                selected = isSelected,
                onClick = { onRangeSelected(range) },
                label = { Text(range.detailLabel(), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(
                    width = 1.dp,
                    brush = if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF80DEEA), Color(0xFFE040FB), Color(0xFFFFD54F))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.08f))
                        )
                    }
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x337C4DFF),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0x14FFFFFF),
                    labelColor = Color.White.copy(alpha = 0.70f)
                )
            )
        }
    }
}

@Composable
private fun StreakBadgeCard(
    currentStreakDays: Int,
    longestStreakDays: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥 ",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Longest Streak: $longestStreakDays days",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun InsightfulStatsSection(uiState: HabitDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Avg Session Length",
                value = formatExactDuration(uiState.averageSessionMinutes),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Longest Session",
                value = formatExactDuration(uiState.longestSessionMinutes),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Best Practice Day",
                value = uiState.bestPracticeDayOfWeek,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Sessions",
                value = "${uiState.sessionCount} sessions",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0x18FFFFFF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.60f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CollapsibleRecentSessions(
    sessions: List<SessionListItem>,
    onSessionClick: (Long) -> Unit,
    onSessionDelete: (Long) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Sessions (${sessions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                if (sessions.isEmpty()) {
                    Text(
                        text = "No sessions logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                } else {
                    sessions.take(30).forEach { session ->
                        SessionRow(
                            session = session,
                            onClick = { onSessionClick(session.id) },
                            onDelete = { onSessionDelete(session.id) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilteredTotalCaption(range: TimeRange, filteredMinutes: Long) {
    Text(
        text = "${range.detailLabel()}: ${formatExactDuration(filteredMinutes)}",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.70f),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

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
                color = Color.White.copy(alpha = 0.60f)
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
                        color = Color.White.copy(alpha = 0.60f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: SessionListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x18FFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatSessionDateLabel(session.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            if (!session.note.isNullOrBlank()) {
                Text(
                    text = session.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatExactDuration(session.durationMinutes.toLong()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete session",
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteHabitConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteHabitConfirmationSheet(
        habitName = habitName,
        onDismiss = onDismiss,
        onConfirmDelete = onConfirm
    )
}

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
