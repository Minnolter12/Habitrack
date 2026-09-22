package com.minnolter.habitrack.ui.screens.create.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minnolter.habitrack.domain.model.HabitCreationDraft

@Composable
fun StepRoutine(
    draft: HabitCreationDraft,
    onDraftChanged: ((HabitCreationDraft) -> HabitCreationDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = draft.scheduleExpectation

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Step 3: Routine & Target Pace",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Define your expected schedule cadence.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = (schedule.sessionDurationMinutes / 60.0).toString(),
                onValueChange = {
                    val hrs = it.toDoubleOrNull() ?: 0.0
                    val mins = (hrs * 60.0).toInt().coerceIn(0, 1440)
                    onDraftChanged { d -> d.copy(scheduleExpectation = schedule.copy(sessionDurationMinutes = mins)) }
                },
                label = { Text("Target Hours/Session") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = schedule.weeklyFrequencyDays.toString(),
                onValueChange = {
                    val days = it.toIntOrNull() ?: 0
                    onDraftChanged { d -> d.copy(scheduleExpectation = schedule.copy(weeklyFrequencyDays = days)) }
                },
                label = { Text("Days/Week") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Projection Card
        val baseline = draft.calculatedBaselineMinutes
        val yearsToMaster = schedule.calculateProjectedYearsToMaster(baseline)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x2200E5FF),
            border = BorderStroke(1.dp, Color(0x4400E5FF))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Projected Roadmap Insights",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Weekly Commitment: ~${schedule.weeklyMinutes / 60} hours/week",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = if (yearsToMaster != null && yearsToMaster > 0f) {
                        "At this pace, you will reach 10,000h Mastery in ~$yearsToMaster years."
                    } else {
                        "Commit to a weekly schedule to calculate your projected timeline."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
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
