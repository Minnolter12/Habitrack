package com.minnolter.habitrack.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minnolter.habitrack.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "habitract_settings")

/**
 * Everything the Settings screen (Section 37) persists. Placed under
 * `data/local/datastore` rather than `ui/screens/settings` — a preferences
 * data source is part of the data layer regardless of which screen happens
 * to be its only current reader, matching how `HabitractDatabase` isn't
 * nested under any one screen either. `SettingsViewModel` still lives where
 * the phase asked for it.
 *
 * [reducedMotionForced]: false means "defer entirely to the system Remove
 * Animations setting" (the Phase 3 default); true forces calmer jelly/
 * shimmer animation even when the system setting is off. It never forces
 * motion *back on* against the system's own accessibility choice — see
 * `LocalReducedMotionPreference` in `ui/components/MotionPreference.kt`,
 * which OR-combines this flag with the live system reading.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reducedMotionForced: Boolean = false,
    val feedbackEnabled: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[THEME_MODE_KEY]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            reducedMotionForced = prefs[REDUCED_MOTION_KEY] ?: false,
            feedbackEnabled = prefs[FEEDBACK_ENABLED_KEY] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    suspend fun setReducedMotionForced(forced: Boolean) {
        context.settingsDataStore.edit { it[REDUCED_MOTION_KEY] = forced }
    }

    suspend fun setFeedbackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[FEEDBACK_ENABLED_KEY] = enabled }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val REDUCED_MOTION_KEY = booleanPreferencesKey("reduced_motion_forced")
        val FEEDBACK_ENABLED_KEY = booleanPreferencesKey("feedback_enabled")
    }
}
