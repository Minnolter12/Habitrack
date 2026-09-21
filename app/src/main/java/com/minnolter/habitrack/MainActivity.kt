package com.minnolter.habitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.datastore.AppSettings
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.ui.components.LocalFeedbackPreference
import com.minnolter.habitrack.ui.components.LocalReducedMotionPreference
import com.minnolter.habitrack.ui.navigation.HabitractNavHost
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModel
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModelFactory
import com.minnolter.habitrack.ui.theme.HabitractTheme

/**
 * The single activity. Its only jobs are: read [HabitractApplication]'s
 * singletons, collect the [AppSettings] that everything downstream needs
 * (theme, reduced motion, feedback), and hand off to [HabitractNavHost] for
 * everything else — no screen-specific logic lives here.
 */
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

/**
 * Reads [AppSettings] once at the app root and fans it out two ways: the
 * [ThemeMode][com.habitract.app.domain.model.ThemeMode] drives
 * [HabitractTheme] directly, while reduced-motion and feedback are provided
 * as [LocalReducedMotionPreference]/[LocalFeedbackPreference] so
 * [com.habitract.app.ui.components.JellyProgressCanvas] and
 * [com.habitract.app.ui.components.LogTimeSheet] pick them up without any
 * change to their own call sites deeper in the tree.
 *
 * A dedicated [SettingsViewModel] instance is created here purely to read
 * [SettingsDataStore.settings] as a [androidx.compose.runtime.State] — it's
 * never handed down; the Settings *screen* creates its own instance, scoped
 * to its own back-stack entry, exactly like every other screen's ViewModel.
 */
@Composable
private fun HabitractRoot(
    repository: HabitractRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: DatabaseBackupManager
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsDataStore, backupManager)
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    HabitractTheme(themeMode = settings.themeMode) {
        CompositionLocalProvider(
            LocalReducedMotionPreference provides settings.reducedMotionForced,
            LocalFeedbackPreference provides settings.feedbackEnabled
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
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
