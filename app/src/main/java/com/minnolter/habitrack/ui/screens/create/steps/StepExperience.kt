package com.minnolter.habitrack.ui.screens.create.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minnolter.habitrack.domain.model.EstimationMode
import com.minnolter.habitrack.domain.model.HabitCreationDraft
import com.minnolter.habitrack.util.formatAccumulatedDuration

@Composable
fun StepExperience(
    draft: HabitCreationDraft,
    onDraftChanged: ((HabitCreationDraft) -> HabitCreationDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Step 2: Prior Experience Breakdown",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Do you have prior hours in this discipline?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Segmented Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(
                label = "Fresh Start",
                isSelected = draft.estimationMode == EstimationMode.ZERO_BASE,
                onClick = { onDraftChanged { it.copy(estimationMode = EstimationMode.ZERO_BASE) } },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                label = "Known Hours",
                isSelected = draft.estimationMode == EstimationMode.DIRECT_HOURS,
                onClick = { onDraftChanged { it.copy(estimationMode = EstimationMode.DIRECT_HOURS) } },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                label = "Calculator",
                isSelected = draft.estimationMode == EstimationMode.HISTORICAL_CALCULATOR,
                onClick = { onDraftChanged { it.copy(estimationMode = EstimationMode.HISTORICAL_CALCULATOR) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (draft.estimationMode) {
            EstimationMode.ZERO_BASE -> {
                Text(
                    text = "Starting from scratch (0 hours). Your journey begins at Stage 1: Just Started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.80f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            EstimationMode.DIRECT_HOURS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (draft.knownHours > 0) draft.knownHours.toString() else "",
                        onValueChange = { val h = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(knownHours = h) } },
                        label = { Text("Hours") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = if (draft.knownMinutes > 0) draft.knownMinutes.toString() else "",
                        onValueChange = { val m = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(knownMinutes = m) } },
                        label = { Text("Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                }
            }

            EstimationMode.HISTORICAL_CALCULATOR -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (draft.yearsPracticed > 0) draft.yearsPracticed.toString() else "",
                        onValueChange = { val y = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(yearsPracticed = y) } },
                        label = { Text("Years Practiced") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = if (draft.monthsPracticed > 0) draft.monthsPracticed.toString() else "",
                        onValueChange = { val m = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(monthsPracticed = m) } },
                        label = { Text("Months") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (draft.sessionsPerWeek > 0) draft.sessionsPerWeek.toString() else "",
                        onValueChange = { val s = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(sessionsPerWeek = s) } },
                        label = { Text("Sessions/Week") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = if (draft.minutesPerSession > 0) draft.minutesPerSession.toString() else "",
                        onValueChange = { val min = it.toIntOrNull() ?: 0; onDraftChanged { d -> d.copy(minutesPerSession = min) } },
                        label = { Text("Mins/Session") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Consistency Factor: ${(draft.consistencyFactor * 100).toInt()}% (Accounts for off-weeks/breaks)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Slider(
                    value = draft.consistencyFactor,
                    onValueChange = { factor -> onDraftChanged { d -> d.copy(consistencyFactor = factor) } },
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF7C4DFF)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculated Result Foundation Card
        val baselineMins = draft.calculatedBaselineMinutes
        val stage = draft.calculatedStage

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x227C4DFF),
            border = BorderStroke(1.dp, Color(0x4480DEEA))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Estimated Baseline Foundation",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = formatAccumulatedDuration(baselineMins),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Unlocks Stage: ${stage.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x447C4DFF) else Color(0x14FFFFFF))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E5FF),
    unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0x14FFFFFF),
    unfocusedContainerColor = Color(0x0AFFFFFF)
)
