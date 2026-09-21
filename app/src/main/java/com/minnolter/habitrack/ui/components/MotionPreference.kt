package com.minnolter.habitrack.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The Settings screen's "Reduce Motion" override (Section 34/37). `false` is
 * the default and means "no app-level override" — [JellyProgressCanvas]
 * still reacts to the live system Remove Animations setting on its own.
 * `true` forces calmer animation everywhere, in addition to whatever the
 * system setting is. Provided once at the app root in `MainActivity` from
 * `SettingsViewModel`'s collected state; every jelly canvas in the tree
 * picks it up automatically without any change to its own call site.
 */
val LocalReducedMotionPreference = staticCompositionLocalOf { false }

/**
 * The Settings screen's sound/haptic feedback toggle. `true` (the default)
 * means logging actions may play a short confirmation haptic; `false`
 * silences them. Read at the handful of call sites that trigger feedback
 * (`LogTimeSheet`, the manual session dialog) rather than centralizing
 * playback, since each caller is best placed to know what just happened.
 */
val LocalFeedbackPreference = staticCompositionLocalOf { true }
