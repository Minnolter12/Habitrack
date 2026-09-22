package com.minnolter.habitrack.ui.screens.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.domain.model.EstimationMode
import com.minnolter.habitrack.domain.model.HabitCreationDraft
import com.minnolter.habitrack.ui.screens.create.steps.IntroStep
import com.minnolter.habitrack.ui.screens.create.steps.StepDiscipline
import com.minnolter.habitrack.util.formatAccumulatedDuration
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitWizardScreen(
    viewModel: CreateHabitViewModel,
    onDismiss: () -> Unit,
    onFinished: (habitId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (uiState.isFirstRunOnboarding) "Welcome Onboarding" else "Create Habit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        if (!uiState.isFirstRunOnboarding) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                val progress = (uiState.stepIndex + 1).toFloat() / uiState.totalSteps.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    color = Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        },
        bottomBar = {
            BottomActionBar(
                stepIndex = uiState.stepIndex,
                totalSteps = uiState.totalSteps,
                isNextEnabled = uiState.isCurrentStepValid && !uiState.isSaving,
                isSaving = uiState.isSaving,
                onBack = { viewModel.goToPreviousStep() },
                onNext = {
                    if (!viewModel.goToNextStep()) {
                        viewModel.saveHabit(onFinished)
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            uiState.validationError?.let { err ->
                Text(
                    text = err,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            AnimatedContent(
                targetState = uiState.stepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }
                },
                label = "wizard_step_transition"
            ) { activeStep ->
                val actualStep = if (uiState.isFirstRunOnboarding) activeStep else activeStep + 1

                when (actualStep) {
                    0 -> IntroStep(onStartClick = { viewModel.goToNextStep() })
                    1 -> StepDiscipline(
                        habitTitle = uiState.draft.habitName,
                        searchQuery = uiState.searchQuery,
                        filteredPresets = uiState.filteredPresets,
                        isCustomHabit = uiState.draft.isCustomHabit,
                        onTitleChanged = { title -> viewModel.updateDraft { it.copy(habitName = title) } },
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        onPresetSelected = { preset -> viewModel.selectPresetActivity(preset) },
                        onEnableCustomHabit = { viewModel.enableCustomHabit() },
                        onCustomImageSelected = { uri -> viewModel.updateDraft { it.copy(imageUrl = uri) } }
                    )
                    2 -> QuestionPracticedBeforePage(
                        habitName = uiState.draft.habitName,
                        onChoiceSelected = { hasPracticed ->
                            viewModel.setHasPracticedBefore(hasPracticed, onFinished)
                        }
                    )
                    3 -> QuestionTimespanPage(
                        habitName = uiState.draft.habitName,
                        years = uiState.draft.timespanYears,
                        months = uiState.draft.timespanMonths,
                        weeks = uiState.draft.timespanWeeks,
                        onYearsChanged = { y -> viewModel.updateDraft { d -> d.copy(timespanYears = y) } },
                        onMonthsChanged = { m -> viewModel.updateDraft { d -> d.copy(timespanMonths = m.coerceIn(0, 11)) } },
                        onWeeksChanged = { w -> viewModel.updateDraft { d -> d.copy(timespanWeeks = w.coerceIn(0, 4)) } }
                    )
                    4 -> QuestionOffTimePage(
                        years = uiState.draft.offTimeYears,
                        months = uiState.draft.offTimeMonths,
                        weeks = uiState.draft.offTimeWeeks,
                        isExceedingGross = !uiState.draft.isOffTimeValid,
                        onYearsChanged = { y -> viewModel.updateDraft { d -> d.copy(offTimeYears = y) } },
                        onMonthsChanged = { m -> viewModel.updateDraft { d -> d.copy(offTimeMonths = m.coerceIn(0, 11)) } },
                        onWeeksChanged = { w -> viewModel.updateDraft { d -> d.copy(offTimeWeeks = w.coerceIn(0, 4)) } }
                    )
                    5 -> QuestionCadencePage(
                        sessionsPerWeek = uiState.draft.sessionsPerWeek,
                        minutesPerSession = uiState.draft.minutesPerSession,
                        onSessionsChanged = { s -> viewModel.updateDraft { d -> d.copy(sessionsPerWeek = s) } },
                        onMinutesChanged = { m -> viewModel.updateDraft { d -> d.copy(minutesPerSession = m) } }
                    )
                    6 -> QuestionConsistencyPage(
                        consistencyPercentage = uiState.draft.consistencyPercentage,
                        onConsistencyChanged = { c -> viewModel.updateDraft { d -> d.copy(consistencyPercentage = c) } }
                    )
                    7 -> FineTuneSliderPage(
                        draft = uiState.draft,
                        onAdjustmentChanged = { deltaHours ->
                            viewModel.updateDraft { d ->
                                d.copy(
                                    manualAdjustmentHours = deltaHours,
                                    estimationMode = EstimationMode.MANUAL_SLIDER
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionPracticedBeforePage(
    habitName: String,
    onChoiceSelected: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Prior Experience Check",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Have you ever practiced \"$habitName\" in your life?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(28.dp))

        ChoiceCard(
            text = "Yes, I have prior hours in this activity",
            subtext = "We will help you calculate your baseline foundation hours.",
            onClick = { onChoiceSelected(true) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ChoiceCard(
            text = "No, I am starting completely from scratch",
            subtext = "Start at 0 logged hours and begin your journey today.",
            onClick = { onChoiceSelected(false) }
        )
    }
}

@Composable
private fun ChoiceCard(text: String, subtext: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0x1EFFFFFF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtext, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun QuestionTimespanPage(
    habitName: String,
    years: Int,
    months: Int,
    weeks: Int,
    onYearsChanged: (Int) -> Unit,
    onMonthsChanged: (Int) -> Unit,
    onWeeksChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Question 1 of 4: Total Timespan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "How much time have you spent practicing $habitName?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = if (years > 0) years.toString() else "",
                onValueChange = { input ->
                    val y = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onYearsChanged(y)
                },
                label = { Text("Years") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = if (months > 0) months.toString() else "",
                onValueChange = { input ->
                    val m = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 11)
                    onMonthsChanged(m)
                },
                label = { Text("Months (0-11)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = if (weeks > 0) weeks.toString() else "",
                onValueChange = { input ->
                    val w = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 4)
                    onWeeksChanged(w)
                },
                label = { Text("Weeks (0-4)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )
        }
    }
}

@Composable
private fun QuestionOffTimePage(
    years: Int,
    months: Int,
    weeks: Int,
    isExceedingGross: Boolean,
    onYearsChanged: (Int) -> Unit,
    onMonthsChanged: (Int) -> Unit,
    onWeeksChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Question 2 of 4: Breaks & Off-Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "How much total off-time, long breaks, or hiatuses did you take during this period?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = if (years > 0) years.toString() else "",
                onValueChange = { input ->
                    val y = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onYearsChanged(y)
                },
                label = { Text("Years") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = if (months > 0) months.toString() else "",
                onValueChange = { input ->
                    val m = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 11)
                    onMonthsChanged(m)
                },
                label = { Text("Months (0-11)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = if (weeks > 0) weeks.toString() else "",
                onValueChange = { input ->
                    val w = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 4)
                    onWeeksChanged(w)
                },
                label = { Text("Weeks (0-4)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )
        }

        if (isExceedingGross) {
            Text(
                text = "Off-time cannot exceed total timespan entered in Question 1.",
                color = Color(0xFFFF5252),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun QuestionCadencePage(
    sessionsPerWeek: Int,
    minutesPerSession: Int,
    onSessionsChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Question 3 of 4: Practice Cadence",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "How often and how long do you usually practice?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = if (sessionsPerWeek > 0) sessionsPerWeek.toString() else "",
                onValueChange = { input ->
                    val s = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 7)
                    onSessionsChanged(s)
                },
                label = { Text("Sessions / Week") },
                placeholder = { Text("e.g. 3") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )

            val sessionHours = minutesPerSession / 60.0
            OutlinedTextField(
                value = if (minutesPerSession > 0) String.format(Locale.US, "%.1f", sessionHours).removeSuffix(".0") else "",
                onValueChange = { input ->
                    val hrs = input.toFloatOrNull() ?: 0f
                    val mins = (hrs * 60f).toInt().coerceIn(0, 1440)
                    onMinutesChanged(mins)
                },
                label = { Text("Hours / Session") },
                placeholder = { Text("e.g. 1.5") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = fieldColors()
            )
        }
    }
}

@Composable
private fun QuestionConsistencyPage(
    consistencyPercentage: Int,
    onConsistencyChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Question 4 of 4: Consistency Factor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What is your typical practice consistency factor?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Accounts for minor missed days, holidays, travel, and sick days.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "$consistencyPercentage% Consistency",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = consistencyPercentage.toFloat(),
            onValueChange = { onConsistencyChanged(it.toInt()) },
            valueRange = 50f..100f,
            steps = 50,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF7C4DFF)
            )
        )
    }
}

@Composable
private fun FineTuneSliderPage(
    draft: HabitCreationDraft,
    onAdjustmentChanged: (Long) -> Unit
) {
    val calculatedBaseHours = draft.calculatedBaseHours
    val fineTunedHours = draft.fineTunedBaseHours
    val stage = draft.calculatedStage

    val minAllowed = draft.minAllowedHours
    val maxAllowed = draft.maxAllowedHours

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Baseline Hours Fine-Tuning",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estimated Foundation: ~${calculatedBaseHours}h. Adjust within allowed bounds (±100h).",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.80f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x227C4DFF),
            border = BorderStroke(1.dp, Color(0x4480DEEA))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calculated Baseline Foundation",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = formatAccumulatedDuration(draft.calculatedBaselineMinutes),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Text(
                    text = "Unlocks Stage: ${stage.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (minAllowed < maxAllowed) {
            Slider(
                value = fineTunedHours.toFloat(),
                onValueChange = { fineTuned ->
                    val delta = fineTuned.toLong() - calculatedBaseHours
                    onAdjustmentChanged(delta)
                },
                valueRange = minAllowed.toFloat()..maxAllowed.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00E5FF),
                    activeTrackColor = Color(0xFF7C4DFF)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Min: ${minAllowed}h", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                Text(text = "Max: ${maxAllowed}h", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    stepIndex: Int,
    totalSteps: Int,
    isNextEnabled: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (stepIndex > 0) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        val isLastStep = stepIndex == totalSteps - 1
        Button(
            onClick = onNext,
            enabled = isNextEnabled && !isSaving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C4DFF),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.12f),
                disabledContentColor = Color.White.copy(alpha = 0.35f)
            )
        ) {
            Text(if (isLastStep) "Create Habit" else "Continue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E5FF),
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0x14FFFFFF),
    unfocusedContainerColor = Color(0x0AFFFFFF)
)
