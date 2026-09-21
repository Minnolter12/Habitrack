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
import androidx.compose.material3.RadioButton
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
import com.minnolter.habitrack.domain.model.BreakUnit
import com.minnolter.habitrack.domain.model.EstimationMode
import com.minnolter.habitrack.domain.model.HabitCreationDraft
import com.minnolter.habitrack.ui.screens.create.steps.IntroStep
import com.minnolter.habitrack.ui.screens.create.steps.StepDiscipline
import com.minnolter.habitrack.util.formatAccumulatedDuration

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
                    3 -> QuestionNumericPage(
                        title = "Question 1 of 4: Prior Years",
                        question = "How many years do you think you have practiced ${uiState.draft.habitName}?",
                        value = if (uiState.draft.yearsPracticed > 0) uiState.draft.yearsPracticed.toString() else "",
                        placeholder = "e.g., 2",
                        onValueChanged = { val y = (it.toIntOrNull() ?: 0).coerceIn(0, 100); viewModel.updateDraft { d -> d.copy(yearsPracticed = y) } }
                    )
                    4 -> QuestionBreakPage(
                        breakValue = uiState.draft.breakValue,
                        breakUnit = uiState.draft.breakUnit,
                        onBreakValueChanged = { v -> viewModel.updateDraft { d -> d.copy(breakValue = v) } },
                        onBreakUnitChanged = { u -> viewModel.updateDraft { d -> d.copy(breakUnit = u) } }
                    )
                    5 -> QuestionNumericPage(
                        title = "Question 3 of 4: Weekly Frequency",
                        question = "How many days per week do you usually practice?",
                        value = if (uiState.draft.sessionsPerWeek > 0) uiState.draft.sessionsPerWeek.toString() else "",
                        placeholder = "e.g., 4",
                        onValueChanged = { val s = (it.toIntOrNull() ?: 0).coerceIn(0, 7); viewModel.updateDraft { d -> d.copy(sessionsPerWeek = s) } }
                    )
                    6 -> QuestionDecimalPage(
                        title = "Question 4 of 4: Session Duration",
                        question = "How many hours per session on average?",
                        value = if (uiState.draft.hoursPerSession > 0f) uiState.draft.hoursPerSession.toString().removeSuffix(".0") else "",
                        placeholder = "e.g., 1.5",
                        onValueChanged = { val hrs = (it.toFloatOrNull() ?: 0f).coerceIn(0f, 24f); viewModel.updateDraft { d -> d.copy(hoursPerSession = hrs) } }
                    )
                    7 -> FineTuneSliderPage(
                        draft = uiState.draft,
                        onHoursChanged = { hours ->
                            viewModel.updateDraft { d ->
                                d.copy(
                                    manualOverrideHours = hours,
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
private fun QuestionBreakPage(
    breakValue: Int,
    breakUnit: BreakUnit,
    onBreakValueChanged: (Int) -> Unit,
    onBreakUnitChanged: (BreakUnit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Question 2 of 4: Off-Time & Breaks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "How long have you taken a break from this activity?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Unit selector chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BreakUnit.entries.forEach { unit ->
                val isSelected = unit == breakUnit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0x447C4DFF) else Color(0x14FFFFFF))
                        .clickable { onBreakUnitChanged(unit) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unit.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = if (breakValue > 0) breakValue.toString() else "",
            onValueChange = { input ->
                val v = (input.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceAtMost(100)
                onBreakValueChanged(v)
            },
            placeholder = { Text("e.g., 6") },
            label = { Text("Break duration in ${breakUnit.label}") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
    }
}

@Composable
private fun QuestionDecimalPage(
    title: String,
    question: String,
    value: String,
    placeholder: String,
    onValueChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val validDecimal = input.filter { it.isDigit() || it == '.' }
                onValueChanged(validDecimal)
            },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
    }
}

@Composable
private fun QuestionNumericPage(
    title: String,
    question: String,
    value: String,
    placeholder: String,
    onValueChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                onValueChanged(digitsOnly)
            },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors()
        )
    }
}

@Composable
private fun FineTuneSliderPage(
    draft: HabitCreationDraft,
    onHoursChanged: (Float) -> Unit
) {
    val baselineMins = draft.calculatedBaselineMinutes
    val currentHours = (baselineMins / 60f).coerceAtMost(10_000f)
    val stage = draft.calculatedStage

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
            text = "Drag the slider to adjust your starting hours. Be honest!",
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
                    text = formatAccumulatedDuration(baselineMins),
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

        Slider(
            value = currentHours,
            onValueChange = onHoursChanged,
            valueRange = 0f..10000f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF7C4DFF)
            )
        )
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
