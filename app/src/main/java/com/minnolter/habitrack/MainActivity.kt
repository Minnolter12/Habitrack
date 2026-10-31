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
        app.syncManager.startSyncing()
        scheduleDailyReminders()

        setContent {
            HabitractRoot(
                repository = app.repository,
                settingsDataStore = app.settingsDataStore,
                backupManager = app.backupManager,
                authManager = app.authManager
            )
        }
    }

    private fun scheduleDailyReminders() {
        val request = androidx.work.PeriodicWorkRequestBuilder<com.minnolter.habitrack.data.notification.DailyReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_duolingo_reminders",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

@Composable
private fun HabitractRoot(
    repository: HabitractRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: DatabaseBackupManager,
    authManager: com.minnolter.habitrack.data.auth.FirebaseAuthManager
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository, settingsDataStore, backupManager, authManager)
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ -> }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            }
        }
    }

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
                    backupManager = backupManager,
                    authManager = authManager
                )
            }
        }
    }
}
