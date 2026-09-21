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

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val reducedMotionForced: Boolean = false,
    val feedbackEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)

class SettingsDataStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.DARK,
            reducedMotionForced = prefs[REDUCED_MOTION_KEY] ?: false,
            feedbackEnabled = prefs[FEEDBACK_ENABLED_KEY] ?: true,
            hasCompletedOnboarding = prefs[HAS_COMPLETED_ONBOARDING_KEY] ?: false
        )
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[HAS_COMPLETED_ONBOARDING_KEY] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE_KEY] = ThemeMode.DARK.name }
    }

    suspend fun setReducedMotionForced(forced: Boolean) {
        context.settingsDataStore.edit { it[REDUCED_MOTION_KEY] = forced }
    }

    suspend fun setFeedbackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[FEEDBACK_ENABLED_KEY] = enabled }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.settingsDataStore.edit { it[HAS_COMPLETED_ONBOARDING_KEY] = completed }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val REDUCED_MOTION_KEY = booleanPreferencesKey("reduced_motion_forced")
        val FEEDBACK_ENABLED_KEY = booleanPreferencesKey("feedback_enabled")
        val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
    }
}
