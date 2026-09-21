package com.minnolter.habitrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.minnolter.habitrack.domain.model.ThemeMode

private val DarkColors = darkColorScheme(
    primary = SeedPrimaryDark,
    onPrimary = SeedOnPrimaryDark,
    primaryContainer = SeedPrimaryContainerDark,
    onPrimaryContainer = SeedOnPrimaryContainerDark,
    secondary = SeedSecondaryDark,
    background = SeedBackgroundDark,
    surface = SeedSurfaceDark
)

/**
 * Habitract is strictly a cosmic Dark Mode app.
 */
@Composable
fun HabitractTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = HabitractTypography,
        content = content
    )
}
