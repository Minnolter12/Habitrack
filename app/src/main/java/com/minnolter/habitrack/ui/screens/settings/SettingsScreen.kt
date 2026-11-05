package com.minnolter.habitrack.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minnolter.habitrack.data.local.datastore.AppSettings
import com.minnolter.habitrack.util.restartApp
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    val onSignInClick: () -> Unit = {
        runCatching {
            val intent = viewModel.getGoogleSignInIntent()
            googleSignInLauncher.launch(intent)
        }.onFailure {
            viewModel.signInWithGoogle(context)
        }
    }

    SettingsScreen(
        settings = settings,
        authState = authState,
        backupState = backupState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAboutClick = onAboutClick,
        onSignInWithGoogleClick = onSignInClick,
        onSignOutClick = viewModel::signOut,
        onFeedbackEnabledChanged = viewModel::setFeedbackEnabled,
        onFluidOpacityChanged = viewModel::setFluidOpacity,
        onImageOpacityChanged = viewModel::setImageOpacity,
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
    onAboutClick: () -> Unit = {},
    onSignInWithGoogleClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onFeedbackEnabledChanged: (Boolean) -> Unit = {},
    onFluidOpacityChanged: (Float) -> Unit = {},
    onImageOpacityChanged: (Float) -> Unit = {},
    onResetAppConfirmed: () -> Unit = {},
    onRestartNow: () -> Unit = {},
    onDismissRestartDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backupInProgress = backupState is BackupOperationState.InProgress
    var showResetDialog1 by remember { mutableStateOf(false) }
    var showResetDialog2 by remember { mutableStateOf(false) }

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
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Floating Sign in with Google Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    if (authState.isAuthenticated) {
                        ListItem(
                            headlineContent = { Text(authState.displayName ?: "Google User", fontWeight = FontWeight.Bold, color = Color.White) },
                            supportingContent = { Text(authState.email ?: "Cloud sync active", color = Color.White.copy(alpha = 0.7f)) },
                            leadingContent = {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color(0xFF00E5FF))
                            },
                            trailingContent = {
                                TextButton(onClick = onSignOutClick) {
                                    Text("Sign Out", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    } else {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color(0xFF00E5FF))
                                Column {
                                    Text("Sign in with Google", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        "Back up habits & sessions and sync across devices",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.70f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSignInWithGoogleClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7C4DFF),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign in with Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Sound & Haptic Feedback Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    ListItem(
                        headlineContent = { Text("Sound & haptic feedback", color = Color.White, fontWeight = FontWeight.Medium) },
                        supportingContent = { Text("A short buzz when you log practice time", color = Color.White.copy(alpha = 0.7f)) },
                        leadingContent = { Icon(Icons.Filled.Vibration, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        trailingContent = {
                            Switch(
                                checked = settings.feedbackEnabled,
                                onCheckedChange = onFeedbackEnabledChanged
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // 3. Floating Opacity Customization Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Card Fluid Fill Opacity", color = Color.White, fontWeight = FontWeight.SemiBold)
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Card Background Image Opacity", color = Color.White, fontWeight = FontWeight.SemiBold)
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
            }

            // 4. Reset Entire App
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    ListItem(
                        headlineContent = { Text("Reset Entire App", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Permanently erases all habits, sessions, and preferences", color = Color.White.copy(alpha = 0.70f)) },
                        leadingContent = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color(0xFFFF5252)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable(enabled = !backupInProgress) { showResetDialog1 = true }
                    )
                }
            }

            // 5. About Screen Navigation
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    ListItem(
                        headlineContent = { Text("About Habitrack", color = Color.White, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("v0.5.0 • Developed by minnolter12 (Dev Madhav)", color = Color.White.copy(alpha = 0.70f)) },
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable(onClick = onAboutClick)
                    )
                }
            }

            if (backupInProgress) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
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
}
