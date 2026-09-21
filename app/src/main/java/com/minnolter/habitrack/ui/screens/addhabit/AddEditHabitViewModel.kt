package com.minnolter.habitrack.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.repository.HabitractRepository
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** A curated, unsaturated accent palette (Section 3: "not overly saturated"). */
val HABIT_ACCENT_COLORS = listOf(
    "#8C7FD6", "#5B9BD5", "#4AAFA5", "#52B788",
    "#C9A227", "#D08770", "#B85C7A", "#6C63B5"
)

data class AddEditHabitUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val colorHex: String = HABIT_ACCENT_COLORS.first(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedHabitId: Long? = null
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

/**
 * Backs the combined Add/Edit Habit destination (Section 30). Scoped
 * deliberately to the straightforward case — name, description, an optional
 * image, and an accent color, saved immediately at zero hours for a new
 * habit. The full "have you practiced this before?" historical-estimate
 * questionnaire (Section 6/7) is a distinct, much larger onboarding flow
 * that Phase 6's cited sections (24, 34–37, 40) don't ask for, so it isn't
 * built here; this screen still fully satisfies "the app should make
 * creating another habit extremely fast" for the common case.
 */
class AddEditHabitViewModel(
    private val habitId: Long?,
    private val repository: HabitractRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHabitUiState(isEditing = habitId != null))
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()

    private var loadedHabit: Habit? = null

    init {
        if (habitId != null) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            viewModelScope.launch {
                val habit = repository.observeHabit(habitId).first()
                loadedHabit = habit
                _uiState.value = if (habit != null) {
                    _uiState.value.copy(
                        isLoading = false,
                        name = habit.name,
                        description = habit.description.orEmpty(),
                        imageUri = habit.imageUri,
                        colorHex = habit.colorHex
                    )
                } else {
                    _uiState.value.copy(isLoading = false, errorMessage = "This habit no longer exists.")
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onImageSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun onColorSelected(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun save() {
        val state = _uiState.value
        val trimmedName = state.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Give this habit a name.")
            return
        }

        _uiState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            val existing = loadedHabit
            val savedId = if (existing != null) {
                repository.updateHabit(
                    existing.copy(
                        name = trimmedName,
                        description = state.description.trim().ifBlank { null },
                        imageUri = state.imageUri,
                        colorHex = state.colorHex
                    )
                )
                existing.id
            } else {
                repository.addHabit(
                    Habit(
                        id = 0L,
                        name = trimmedName,
                        createdAt = Instant.now(),
                        imageUri = state.imageUri,
                        colorHex = state.colorHex,
                        sortOrder = 0, // overwritten by HabitractRepositoryImpl.addHabit
                        description = state.description.trim().ifBlank { null }
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isSaving = false, savedHabitId = savedId)
        }
    }
}

class AddEditHabitViewModelFactory(
    private val habitId: Long?,
    private val repository: HabitractRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AddEditHabitViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return AddEditHabitViewModel(habitId, repository) as T
    }
}
