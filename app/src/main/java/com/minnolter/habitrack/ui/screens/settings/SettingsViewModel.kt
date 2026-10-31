package com.minnolter.habitrack.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minnolter.habitrack.data.auth.AuthUserState
import com.minnolter.habitrack.data.auth.FirebaseAuthManager
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.datastore.AppSettings
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.model.ThemeMode
import com.minnolter.habitrack.domain.repository.HabitractRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BackupOperationState {
    data object Idle : BackupOperationState
    data object InProgress : BackupOperationState
    data class ExportSucceeded(val message: String) : BackupOperationState
    data class ImportSucceeded(val message: String, val pendingRestart: Boolean) : BackupOperationState
    data class Failed(val message: String) : BackupOperationState
}

class SettingsViewModel(
    private val repository: HabitractRepository,
    private val settingsDataStore: SettingsDataStore,
    private val backupManager: DatabaseBackupManager,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = AppSettings()
    )

    val authState: StateFlow<AuthUserState> = authManager.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = AuthUserState()
    )

    private val _backupState = MutableStateFlow<BackupOperationState>(BackupOperationState.Idle)
    val backupState: StateFlow<BackupOperationState> = _backupState.asStateFlow()

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            authManager.signInWithGoogle(activityContext)
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun setReducedMotionForced(forced: Boolean) {
        viewModelScope.launch { settingsDataStore.setReducedMotionForced(forced) }
    }

    fun setFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setFeedbackEnabled(enabled) }
    }

    fun setFluidOpacity(opacity: Float) {
        viewModelScope.launch { settingsDataStore.setFluidOpacity(opacity) }
    }

    fun setImageOpacity(opacity: Float) {
        viewModelScope.launch { settingsDataStore.setImageOpacity(opacity) }
    }

    fun exportDatabase(destinationUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupOperationState.InProgress
            backupManager.exportDatabase(destinationUri).fold(
                onSuccess = {
                    _backupState.value = BackupOperationState.ExportSucceeded("Backup saved.")
                },
                onFailure = { error ->
                    _backupState.value = BackupOperationState.Failed(error.message ?: "Export failed.")
                }
            )
        }
    }

    fun importDatabase(sourceUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupOperationState.InProgress
            backupManager.importDatabase(sourceUri).fold(
                onSuccess = {
                    _backupState.value = BackupOperationState.ImportSucceeded(
                        message = "Backup restored. Restart to finish.",
                        pendingRestart = true
                    )
                },
                onFailure = { error ->
                    _backupState.value = BackupOperationState.Failed(error.message ?: "Import failed.")
                }
            )
        }
    }

    fun resetEntireApp(onFinished: () -> Unit) {
        viewModelScope.launch {
            _backupState.value = BackupOperationState.InProgress
            repository.resetAllData()
            settingsDataStore.clearAllSettings()
            _backupState.value = BackupOperationState.ImportSucceeded(
                message = "App reset complete. Restart to finish.",
                pendingRestart = true
            )
            onFinished()
        }
    }

    fun dismissBackupState() {
        _backupState.value = BackupOperationState.Idle
    }
}

class SettingsViewModelFactory(
    private val repository: HabitractRepository,
    private val settingsDataStore: SettingsDataStore,
    private val backupManager: DatabaseBackupManager,
    private val authManager: FirebaseAuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(repository, settingsDataStore, backupManager, authManager) as T
    }
}
