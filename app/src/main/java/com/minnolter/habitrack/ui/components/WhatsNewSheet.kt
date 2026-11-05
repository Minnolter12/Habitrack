package com.minnolter.habitrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ReleaseNoteGroup(
    val version: String,
    val versionCode: Int,
    val title: String,
    val items: List<String>
)

val CUMULATIVE_RELEASE_NOTES: List<ReleaseNoteGroup> = listOf(
    ReleaseNoteGroup(
        version = "0.6.0",
        versionCode = 600,
        title = "v0.6.0 • Translucent Pill Nav & 3D Depth Expansion",
        items = listOf(
            "💊 Translucent Pill Bottom Bar: Floating glassmorphic navigation with tactile haptics.",
            "🌌 3D Card Depth Expansion: Immersive 600ms spring-animated 3D zoom when opening cards.",
            "🔐 Google Sign-In & Firestore Sync: Real Google Account authentication and cloud sync.",
            "📱 Squarish FAB: Compact 18dp squarish FAB floating above bottom navigation bar.",
            "ℹ️ Dedicated About Page: Redesigned About screen with GitHub integration."
        )
    ),
    ReleaseNoteGroup(
        version = "0.5.0",
        versionCode = 500,
        title = "v0.5.0 • Reminders & Smart Widget",
        items = listOf(
            "🦉 Duolingo-style Reminders: Playful daily prompts if you haven't logged practice hours.",
            "📱 Smart Home Screen Widget: Detects your active habit and displays hours invested.",
            "📋 Dynamic Release Notes: Tailored documentation based on your upgrade path."
        )
    ),
    ReleaseNoteGroup(
        version = "0.4.0",
        versionCode = 400,
        title = "v0.4.0 • Google Sign-In & Cloud Sync",
        items = listOf(
            "☁️ Google Sign-In: Connect your Google Account for cloud backups.",
            "🔄 Cloud Firestore Sync: Seamlessly sync habits & sessions across devices.",
            "🛡️ Local-First Protection: Existing offline data is preserved without data loss."
        )
    ),
    ReleaseNoteGroup(
        version = "0.3.0",
        versionCode = 300,
        title = "v0.3.0 • Card Opacity & Completion Projections",
        items = listOf(
            "📈 Completion Projections: Estimate projected completion dates for Master (10,000h).",
            "🍩 Donut Distribution Charts: Time of Day and Day of Week breakdowns.",
            "🎨 Card Opacity Sliders: Customize liquid fill and background picture transparency.",
            "ℹ️ Developer About Page: Detailed app information with GitHub repository links."
        )
    ),
    ReleaseNoteGroup(
        version = "0.2.0",
        versionCode = 200,
        title = "v0.2.0 • Period Cycling & Focus Rhythm",
        items = listOf(
            "🗓️ Period Cycling: Use < and > controls to cycle through past weeks, months, and years.",
            "🎯 Prime Focus Rhythm: Detects your peak focus hours automatically.",
            "⚡ Weekly Velocity: Real-time velocity tracking and 30-day active rate."
        )
    ),
    ReleaseNoteGroup(
        version = "0.1.0",
        versionCode = 100,
        title = "v0.1.0 • Core 10,000-Hour Engine",
        items = listOf(
            "⏳ 10,000-Hour Rule Engine: 14-stage progression system from Just Started to Master.",
            "🌊 Holographic Jelly Fill: Signature liquid cards that fill as you log practice time.",
            "📊 Dynamic Range Filters: Track practice across Today, Week, Month, Year, and Lifetime."
        )
    )
)

fun parseVersionCode(version: String): Int {
    if (version.isBlank()) return 0
    val parts = version.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return major * 100 + minor * 10 + patch
}

fun getReleaseNotesForUpgrade(lastSeenVersion: String): List<ReleaseNoteGroup> {
    if (lastSeenVersion.isBlank()) return CUMULATIVE_RELEASE_NOTES.take(1)
    val lastCode = parseVersionCode(lastSeenVersion)
    val filtered = CUMULATIVE_RELEASE_NOTES.filter { it.versionCode > lastCode }
    return filtered.ifEmpty { CUMULATIVE_RELEASE_NOTES.take(1) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    lastSeenVersion: String,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    val releaseNotes = getReleaseNotesForUpgrade(lastSeenVersion)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1B1822),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀", style = MaterialTheme.typography.titleMedium)
                }
                Column {
                    Text(
                        text = "What's New in Habitrack",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Updates since version ${if (lastSeenVersion.isBlank()) "0.4.1" else lastSeenVersion}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF00E5FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(16.dp))

            releaseNotes.forEachIndexed { index, group ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                group.items.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Got It! Start Exploring",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
