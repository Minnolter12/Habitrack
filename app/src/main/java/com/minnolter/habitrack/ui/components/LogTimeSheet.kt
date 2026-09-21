package com.minnolter.habitrack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

/**
 * The quick-logging bottom sheet from Section 16 — deliberately minimal so
 * recording a session never takes more than one or two taps. The four
 * presets (15/30/45/60 min) log immediately on tap; "Custom duration"
 * reveals hours + minutes fields for anything else, matching the spec's
 * "7 minutes / 23 minutes / 1h 12m" style examples.
 *
 * [onConfirm] is called with a positive minute count exactly once per
 * logged session; this composable never talks to the repository directly,
 * keeping it reusable from any screen that wants a quick-log affordance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTimeSheet(
    habitName: String,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val haptics = LocalHapticFeedback.current
    val feedbackEnabled = LocalFeedbackPreference.current
    val confirmWithFeedback: (Int) -> Unit = { minutes ->
        if (feedbackEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onConfirm(minutes)
    }

    var showCustomFields by rememberSaveable { mutableStateOf(false) }
    var hoursField by rememberSaveable(stateSaver = TextFieldValueSaver) {
        mutableStateOf(TextFieldValue(""))
    }
    var minutesField by rememberSaveable(stateSaver = TextFieldValueSaver) {
        mutableStateOf(TextFieldValue(""))
    }

    val customTotalMinutes = remember(hoursField, minutesField) {
        val hours = hoursField.text.toIntOrNull() ?: 0
        val minutes = minutesField.text.toIntOrNull() ?: 0
        hours * 60 + minutes
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Log Practice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = habitName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_MINUTES.forEach { preset ->
                    FilterChip(
                        selected = false,
                        onClick = { confirmWithFeedback(preset) },
                        label = { Text(presetLabel(preset)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { showCustomFields = !showCustomFields }) {
                Text(if (showCustomFields) "Hide custom duration" else "Custom duration")
            }

            AnimatedVisibility(visible = showCustomFields) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = hoursField,
                            onValueChange = { hoursField = it.filterToDigits() },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minutesField,
                            onValueChange = { minutesField = it.filterToDigits() },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { confirmWithFeedback(customTotalMinutes) },
                        enabled = customTotalMinutes > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (customTotalMinutes > 0) {
                                "Log ${presetLabel(customTotalMinutes)}"
                            } else {
                                "Enter a duration"
                            }
                        )
                    }
                }
            }
        }
    }
}

private val PRESET_MINUTES = listOf(15, 30, 45, 60)

private fun presetLabel(minutes: Int): String = when {
    minutes == 60 -> "1 hour"
    minutes % 60 == 0 -> "${minutes / 60} hours"
    minutes < 60 -> "$minutes min"
    else -> "${minutes / 60}h ${minutes % 60}m"
}

/** Keeps only digit characters, so the numeric fields can never hold invalid input. */
private fun TextFieldValue.filterToDigits(): TextFieldValue {
    val digitsOnly = text.filter { it.isDigit() }
    return copy(text = digitsOnly)
}

private val TextFieldValueSaver = androidx.compose.runtime.saveable.Saver<TextFieldValue, String>(
    save = { it.text },
    restore = { TextFieldValue(it) }
)
