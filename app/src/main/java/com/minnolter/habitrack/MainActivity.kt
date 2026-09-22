package com.minnolter.habitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.ui.components.LocalFeedbackPreference
import com.minnolter.habitrack.ui.components.LocalReducedMotionPreference
import com.minnolter.habitrack.ui.navigation.HabitractNavHost
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModel
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModelFactory
import com.minnolter.habitrack.ui.theme.HabitractTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HabitractApplication

        setContent {
            HabitractRoot(
                repository = app.repository,
                settingsDataStore = app.settingsDataStore,
                backupManager = app.backupManager
            )
        }
    }
}

@Composable
private fun HabitractRoot(
    repository: HabitractRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: DatabaseBackupManager
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository, settingsDataStore, backupManager)
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val cosmicBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF16131D),
            Color(0xFF0F0C15),
            Color(0xFF08060B)
        )
    )

    HabitractTheme(themeMode = settings.themeMode) {
        CompositionLocalProvider(
            LocalReducedMotionPreference provides settings.reducedMotionForced,
            LocalFeedbackPreference provides settings.feedbackEnabled
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cosmicBackground)
            ) {
                HabitractNavHost(
                    repository = repository,
                    settingsDataStore = settingsDataStore,
                    backupManager = backupManager
                )
            }
        }
    }
}
