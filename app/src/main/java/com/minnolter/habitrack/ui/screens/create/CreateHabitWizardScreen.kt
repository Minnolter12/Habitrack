package com.minnolter.habitrack.ui.screens.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.ui.screens.create.steps.IntroStep
import com.minnolter.habitrack.ui.screens.create.steps.StepDiscipline
import com.minnolter.habitrack.ui.screens.create.steps.StepExperience
import com.minnolter.habitrack.ui.screens.create.steps.StepRoutine
import com.minnolter.habitrack.ui.screens.create.steps.StepVisuals

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
                        onTitleChanged = { title -> viewModel.updateDraft { it.copy(habitName = title) } },
                        onCategorySelected = { cat -> viewModel.updateDraft { it.copy(category = cat) } },
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        onPresetSelected = { preset -> viewModel.selectPresetActivity(preset) }
                    )
                    2 -> StepExperience(
                        draft = uiState.draft,
                        onDraftChanged = { transform -> viewModel.updateDraft(transform) }
                    )
                    3 -> StepRoutine(
                        draft = uiState.draft,
                        onDraftChanged = { transform -> viewModel.updateDraft(transform) }
                    )
                    4 -> StepVisuals(
                        draft = uiState.draft,
                        onDraftChanged = { transform -> viewModel.updateDraft(transform) }
                    )
                }
            }
        }
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
