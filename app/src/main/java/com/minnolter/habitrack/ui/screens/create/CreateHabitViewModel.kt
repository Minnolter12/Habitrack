package com.minnolter.habitrack.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minnolter.habitrack.data.local.PresetActivitiesDatabase
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.model.EstimationMode
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.HabitCreationDraft
import com.minnolter.habitrack.domain.model.PracticeSession
import com.minnolter.habitrack.domain.model.PresetActivity
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.repository.HabitractRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class CreateHabitWizardUiState(
    val isFirstRunOnboarding: Boolean = false,
    val stepIndex: Int = 0,
    val totalSteps: Int = 7,
    val draft: HabitCreationDraft = HabitCreationDraft(),
    val searchQuery: String = "",
    val filteredPresets: List<PresetActivity> = PresetActivitiesDatabase.ALL_PRESETS,
    val isSaving: Boolean = false,
    val validationError: String? = null
) {
    val currentStagePreview: ProgressionStage get() = draft.calculatedStage

    /** Returns whether the Continue button should be enabled for the current step. */
    val isCurrentStepValid: Boolean
        get() {
            val titleStepIdx = if (isFirstRunOnboarding) 1 else 0
            if (stepIndex == titleStepIdx) {
                return draft.habitName.isNotBlank()
            }
            return true
        }
}

class CreateHabitViewModel(
    val isFirstRunOnboarding: Boolean,
    private val repository: HabitractRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val totalStepsCount = if (isFirstRunOnboarding) 8 else 7

    private val _uiState = MutableStateFlow(
        CreateHabitWizardUiState(
            isFirstRunOnboarding = isFirstRunOnboarding,
            totalSteps = totalStepsCount,
            filteredPresets = PresetActivitiesDatabase.ALL_PRESETS
        )
    )
    val uiState: StateFlow<CreateHabitWizardUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        val category = _uiState.value.draft.category
        val filtered = PresetActivitiesDatabase.search(query, category)
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredPresets = filtered
            )
        }
    }

    fun selectPresetActivity(preset: PresetActivity) {
        _uiState.update {
            it.copy(
                searchQuery = preset.name,
                draft = it.draft.copy(
                    habitName = preset.name,
                    category = preset.category,
                    colorHex = preset.defaultColorHex,
                    imageUrl = preset.imageUrl,
                    isCustomHabit = false
                )
            )
        }
    }

    fun enableCustomHabit() {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(
                    habitName = "",
                    imageUrl = null,
                    isCustomHabit = true
                )
            )
        }
    }

    fun updateDraft(transform: (HabitCreationDraft) -> HabitCreationDraft) {
        _uiState.update { it.copy(draft = transform(it.draft), validationError = null) }
    }

    fun setHasPracticedBefore(hasPracticed: Boolean, onFinished: (habitId: Long) -> Unit) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(
                    hasPracticedBefore = hasPracticed,
                    estimationMode = if (hasPracticed) EstimationMode.HISTORICAL_CALCULATOR else EstimationMode.ZERO_BASE
                )
            )
        }

        if (!hasPracticed) {
            // Directly save habit with 0 hours logged!
            saveHabit(onFinished)
        } else {
            goToNextStep()
        }
    }

    fun goToNextStep(): Boolean {
        val state = _uiState.value

        val titleStepIdx = if (state.isFirstRunOnboarding) 1 else 0
        if (state.stepIndex == titleStepIdx) {
            if (state.draft.habitName.isBlank()) {
                _uiState.update { it.copy(validationError = "Please enter or select a habit name") }
                return false
            }
        }

        if (state.stepIndex < state.totalSteps - 1) {
            _uiState.update { it.copy(stepIndex = it.stepIndex + 1, validationError = null) }
            return true
        }
        return false
    }

    fun goToPreviousStep() {
        if (_uiState.value.stepIndex > 0) {
            _uiState.update { it.copy(stepIndex = _uiState.value.stepIndex - 1, validationError = null) }
        }
    }

    fun saveHabit(onFinished: (habitId: Long) -> Unit) {
        if (_uiState.value.isSaving) return

        val state = _uiState.value
        val draft = state.draft

        if (draft.habitName.isBlank()) {
            _uiState.update { it.copy(validationError = "Habit name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val newHabit = Habit(
                id = 0L,
                name = draft.habitName.trim(),
                createdAt = Instant.now(),
                imageUri = draft.imageUrl,
                colorHex = draft.colorHex,
                sortOrder = 0,
                description = draft.category.displayName
            )

            val newHabitId = repository.addHabit(newHabit)

            // If user has practiced before and baseline > 0, log initial foundation practice session
            val baselineMinutes = draft.calculatedBaselineMinutes
            if (draft.hasPracticedBefore && baselineMinutes > 0L) {
                val historicalSession = PracticeSession(
                    id = 0L,
                    habitId = newHabitId,
                    durationMinutes = baselineMinutes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    timestamp = Instant.now(),
                    note = "Historical baseline foundation"
                )
                repository.logSession(historicalSession)
            }

            if (state.isFirstRunOnboarding) {
                settingsDataStore.setHasCompletedOnboarding(true)
            }

            _uiState.update { it.copy(isSaving = false) }
            onFinished(newHabitId)
        }
    }
}

class CreateHabitViewModelFactory(
    private val isFirstRunOnboarding: Boolean,
    private val repository: HabitractRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateHabitViewModel(isFirstRunOnboarding, repository, settingsDataStore) as T
    }
}
