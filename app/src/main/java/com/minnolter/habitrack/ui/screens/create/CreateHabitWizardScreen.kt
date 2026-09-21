package com.minnolter.habitrack.ui.screens.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
            Column {
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
                        .padding(horizontal = 20.dp),
                    color = Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        },
        bottomBar = {
            BottomActionBar(
                stepIndex = uiState.stepIndex,
                totalSteps = uiState.totalSteps,
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
                        selectedCategory = uiState.draft.category,
                        searchQuery = uiState.searchQuery,
                        filteredPresets = uiState.filteredPresets,
                        isCustomHabit = uiState.draft.isCustomHabit,
                        onTitleChanged = { title -> viewModel.updateDraft { it.copy(habitName = title) } },
                        onCategorySelected = { cat -> viewModel.updateDraft { it.copy(category = cat) } },
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        onPresetSelected = { preset -> viewModel.selectPresetActivity(preset) },
                        onEnableCustomHabit = { viewModel.enableCustomHabit() },
                        onCustomImageSelected = { uri -> viewModel.updateDraft { it.copy(imageUrl = uri) } }
                    )
                    2 -> QuestionNumericPage(
                        title = "Question 1 of 4: Prior Years",
                        question = "How many years do you think you have practiced?",
                        value = if (uiState.draft.yearsPracticed > 0) uiState.draft.yearsPracticed.toString() else "",
                        placeholder = "e.g., 2",
                        onValueChanged = { val y = (it.toIntOrNull() ?: 0).coerceIn(0, 100); viewModel.updateDraft { d -> d.copy(yearsPracticed = y, estimationMode = EstimationMode.HISTORICAL_CALCULATOR) } }
                    )
                    3 -> QuestionNumericPage(
                        title = "Question 2 of 4: Additional Months",
                        question = "How many additional months (on top of years)?",
                        value = if (uiState.draft.monthsPracticed > 0) uiState.draft.monthsPracticed.toString() else "",
                        placeholder = "e.g., 6",
                        onValueChanged = { val m = (it.toIntOrNull() ?: 0).coerceIn(0, 11); viewModel.updateDraft { d -> d.copy(monthsPracticed = m) } }
                    )
                    4 -> QuestionNumericPage(
                        title = "Question 3 of 4: Weekly Frequency",
                        question = "How many days per week do you usually practice?",
                        value = if (uiState.draft.sessionsPerWeek > 0) uiState.draft.sessionsPerWeek.toString() else "",
                        placeholder = "e.g., 4",
                        onValueChanged = { val s = (it.toIntOrNull() ?: 0).coerceIn(0, 7); viewModel.updateDraft { d -> d.copy(sessionsPerWeek = s) } }
                    )
                    5 -> QuestionNumericPage(
                        title = "Question 4 of 4: Session Duration",
                        question = "How many minutes per session on average?",
                        value = if (uiState.draft.minutesPerSession > 0) uiState.draft.minutesPerSession.toString() else "",
                        placeholder = "e.g., 45",
                        onValueChanged = { val min = (it.toIntOrNull() ?: 0).coerceIn(0, 1440); viewModel.updateDraft { d -> d.copy(minutesPerSession = min) } }
                    )
                    6 -> FineTuneSliderPage(
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x14FFFFFF),
                unfocusedContainerColor = Color(0x0AFFFFFF)
            )
        )
    }
}

@Composable
private fun FineTuneSliderPage(
    draft: HabitCreationDraft,
    onHoursChanged: (Float) -> Unit
) {
    val baselineMins = draft.calculatedBaselineMinutes
    val currentHours = (baselineMins / 60f).coerceAtMost(50_000f)
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
            valueRange = 0f..1000f,
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
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
            enabled = !isSaving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C4DFF),
                contentColor = Color.White
            )
        ) {
            Text(if (isLastStep) "Create Habit" else "Continue", fontWeight = FontWeight.Bold)
        }
    }
}
