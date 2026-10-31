package com.minnolter.habitrack.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.data.local.datastore.AppSettings
import com.minnolter.habitrack.util.restartApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onReorderHabitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
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
            is BackupOperationState.ImportSucceeded -> null
            BackupOperationState.Idle, BackupOperationState.InProgress -> null
        }
        if (message != null) {
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.dismissBackupState()
        }
    }

    SettingsScreen(
        settings = settings,
        authState = authState,
        backupState = backupState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSignInWithGoogleClick = { viewModel.signInWithGoogle(context) },
        onSignOutClick = viewModel::signOut,
        onReducedMotionChanged = viewModel::setReducedMotionForced,
        onFeedbackChanged = viewModel::setFeedbackEnabled,
        onFluidOpacityChanged = viewModel::setFluidOpacity,
        onImageOpacityChanged = viewModel::setImageOpacity,
        onReorderHabitsClick = onReorderHabitsClick,
        onExportClick = { exportLauncher.launch(defaultBackupFileName()) },
        onImportClick = {
            importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*"))
        },
        onResetAppConfirmed = {
            viewModel.resetEntireApp { restartApp(context) }
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
    authState: com.minnolter.habitrack.data.auth.AuthUserState = com.minnolter.habitrack.data.auth.AuthUserState(),
    backupState: BackupOperationState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSignInWithGoogleClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onReducedMotionChanged: (Boolean) -> Unit,
    onFeedbackChanged: (Boolean) -> Unit,
    onFluidOpacityChanged: (Float) -> Unit = {},
    onImageOpacityChanged: (Float) -> Unit = {},
    onReorderHabitsClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResetAppConfirmed: () -> Unit,
    onRestartNow: () -> Unit,
    onDismissRestartDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backupInProgress = backupState is BackupOperationState.InProgress
    var showResetDialog1 by remember { mutableStateOf(false) }
    var showResetDialog2 by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            item { SectionHeader("Account & Cloud Sync") }
            if (authState.isAuthenticated) {
                item {
                    ListItem(
                        headlineContent = { Text(authState.displayName ?: "Google User", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(authState.email ?: "Cloud sync active") },
                        leadingContent = {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color(0xFF00E5FF))
                        },
                        trailingContent = {
                            TextButton(onClick = onSignOutClick) {
                                Text("Sign Out", color = Color(0xFFFF5252))
                            }
                        }
                    )
                }
            } else {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ListItem(
                            headlineContent = { Text("Sign in with Google", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Back up your habits & sessions and sync across devices") },
                            leadingContent = {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color(0xFF00E5FF))
                            },
                            modifier = Modifier.clickable(onClick = onSignInWithGoogleClick)
                        )
                        Button(
                            onClick = onSignInWithGoogleClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C4DFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("Sign in with Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { HorizontalDivider() }
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
            item { SectionHeader("Accessibility") }
            item {
                ListItem(
                    headlineContent = { Text("Reduce motion") },
                    supportingContent = { Text("Calms the jelly animation & 3D tilt, in addition to your system setting") },
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
            item { SectionHeader("Card Opacity & Customization") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Card Fluid Fill Opacity", fontWeight = FontWeight.Medium)
                        Text(
                            text = "${(settings.fluidOpacity * 100).toInt()}%",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.fluidOpacity,
                        onValueChange = onFluidOpacityChanged,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Card Background Image Opacity", fontWeight = FontWeight.Medium)
                        Text(
                            text = "${(settings.imageOpacity * 100).toInt()}%",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.imageOpacity,
                        onValueChange = onImageOpacityChanged,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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

            item { HorizontalDivider() }
            item { SectionHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("About Habitrack", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("v0.3.0 • Developed by Dev Madhav • GitHub") },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF00E5FF)) },
                    modifier = Modifier.clickable { showAboutDialog = true }
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Danger Zone", color = Color(0xFFFF5252)) }
            item {
                ListItem(
                    headlineContent = { Text("Reset Entire App", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Permanently erases all habits, sessions, and preferences") },
                    leadingContent = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color(0xFFFF5252)) },
                    modifier = Modifier.clickable(enabled = !backupInProgress) { showResetDialog1 = true }
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

    // Reset Confirmation Dialog 1
    if (showResetDialog1) {
        AlertDialog(
            onDismissRequest = { showResetDialog1 = false },
            title = { Text("Reset Habitrack Entirely?") },
            text = { Text("Are you sure you want to reset the app? All logged practice hours, statistics, and habits will be wiped.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog1 = false
                    showResetDialog2 = true
                }) {
                    Text("Continue Reset", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog1 = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Confirmation Dialog 2 (FINAL WARNING)
    if (showResetDialog2) {
        AlertDialog(
            onDismissRequest = { showResetDialog2 = false },
            title = { Text("FINAL WARNING", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) },
            text = { Text("This operation CANNOT BE UNDONE. Are you 100% certain you want to erase all data and reset onboarding?") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog2 = false
                    onResetAppConfirmed()
                }) {
                    Text("Erase & Reset Now", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog2 = false }) { Text("Cancel") }
            }
        )
    }

    if (backupState is BackupOperationState.ImportSucceeded && backupState.pendingRestart) {
        AlertDialog(
            onDismissRequest = onDismissRestartDialog,
            title = { Text("Operation Complete") },
            text = { Text("Habitrack needs to restart to finish.") },
            confirmButton = { TextButton(onClick = onRestartNow) { Text("Restart now") } },
            dismissButton = { TextButton(onClick = onDismissRestartDialog) { Text("Later") } }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/Minnolter12/Habitrack"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF)
                )
                Text("About Habitrack", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Habitrack",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Version 0.3.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Time shapes you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Crafted with ❤ by Dev Madhav",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "An open-source, privacy-first 10,000-hour mastery & habit tracker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.70f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Visit GitHub Repository", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF00E5FF))
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String, color: Color = Color(0xFF00E5FF)) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

private fun defaultBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "habitract_backup_$timestamp.db"
}
