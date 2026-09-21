package com.minnolter.habitrack.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.data.local.datastore.AppSettings
import com.minnolter.habitrack.domain.model.ThemeMode
import com.minnolter.habitrack.util.restartApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Stateful entry point: owns the [SettingsViewModel] subscription, the SAF
 * export/import launchers, and the restart-required dialog after a
 * successful restore.
 */
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onReorderHabitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let(viewModel::exportDatabase) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importDatabase) }

    LaunchedEffect(backupState) {
        val message = when (val state = backupState) {
            is BackupOperationState.ExportSucceeded -> state.message
            is BackupOperationState.Failed -> state.message
            is BackupOperationState.ImportSucceeded -> null // handled by the restart dialog instead
            BackupOperationState.Idle, BackupOperationState.InProgress -> null
        }
        if (message != null) {
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.dismissBackupState()
        }
    }

    SettingsScreen(
        settings = settings,
        backupState = backupState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onThemeModeSelected = viewModel::setThemeMode,
        onReducedMotionChanged = viewModel::setReducedMotionForced,
        onFeedbackChanged = viewModel::setFeedbackEnabled,
        onReorderHabitsClick = onReorderHabitsClick,
        onExportClick = { exportLauncher.launch(defaultBackupFileName()) },
        onImportClick = {
            importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*"))
        },
        onRestartNow = { restartApp(context) },
        onDismissRestartDialog = viewModel::dismissBackupState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    backupState: BackupOperationState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onFeedbackChanged: (Boolean) -> Unit,
    onReorderHabitsClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onRestartNow: () -> Unit,
    onDismissRestartDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backupInProgress = backupState is BackupOperationState.InProgress

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            item { SectionHeader("Organization") }
            item {
                ListItem(
                    headlineContent = { Text("Reorder habits") },
                    supportingContent = { Text("Drag to change the order shown on Home") },
                    leadingContent = { Icon(Icons.Filled.Reorder, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onReorderHabitsClick)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Appearance") }
            items(ThemeMode.entries) { mode ->
                ListItem(
                    headlineContent = { Text(mode.label()) },
                    leadingContent = {
                        RadioButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeSelected(mode) }
                        )
                    },
                    modifier = Modifier.clickable { onThemeModeSelected(mode) }
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Accessibility") }
            item {
                ListItem(
                    headlineContent = { Text("Reduce motion") },
                    supportingContent = { Text("Calms the jelly animation, in addition to your system setting") },
                    trailingContent = {
                        Switch(
                            checked = settings.reducedMotionForced,
                            onCheckedChange = onReducedMotionChanged
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Sound & haptic feedback") },
                    supportingContent = { Text("A short buzz when you log practice time") },
                    leadingContent = { Icon(Icons.Filled.Vibration, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = settings.feedbackEnabled,
                            onCheckedChange = onFeedbackChanged
                        )
                    }
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Backup") }
            item {
                ListItem(
                    headlineContent = { Text("Export backup") },
                    supportingContent = { Text("Save all habits and sessions to a file") },
                    leadingContent = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                    modifier = Modifier.clickable(enabled = !backupInProgress, onClick = onExportClick)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Restore from backup") },
                    supportingContent = { Text("Replaces everything currently on this device") },
                    leadingContent = { Icon(Icons.Filled.CloudDownload, contentDescription = null) },
                    modifier = Modifier.clickable(enabled = !backupInProgress, onClick = onImportClick)
                )
            }
            if (backupInProgress) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (backupState is BackupOperationState.ImportSucceeded && backupState.pendingRestart) {
        AlertDialog(
            onDismissRequest = onDismissRestartDialog,
            title = { Text("Restore complete") },
            text = { Text("Habitract needs to restart to load the restored data.") },
            confirmButton = { TextButton(onClick = onRestartNow) { Text("Restart now") } },
            dismissButton = { TextButton(onClick = onDismissRestartDialog) { Text("Later") } }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun defaultBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "habitract_backup_$timestamp.db"
}
