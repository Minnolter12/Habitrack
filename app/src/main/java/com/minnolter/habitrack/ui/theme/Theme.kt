package com.minnolter.habitrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.minnolter.habitrack.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = SeedPrimaryLight,
    onPrimary = SeedOnPrimaryLight,
    primaryContainer = SeedPrimaryContainerLight,
    onPrimaryContainer = SeedOnPrimaryContainerLight,
    secondary = SeedSecondaryLight,
    background = SeedBackgroundLight,
    surface = SeedSurfaceLight
)

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
 * Resolves [themeMode] (Section 37: System default / Light / Dark) against
 * the device's own dark-theme state, applies Material You dynamic color on
 * Android 12+ when available, and falls back to the static seed palette in
 * [Color.kt] everywhere else — including explicitly honoring a user's Light
 * or Dark choice over a dynamic scheme that wouldn't match it.
 */
@Composable
fun HabitractTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColorSupported && useDarkTheme -> dynamicDarkColorScheme(context)
        dynamicColorSupported && !useDarkTheme -> dynamicLightColorScheme(context)
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitractTypography,
        content = content
    )
}
