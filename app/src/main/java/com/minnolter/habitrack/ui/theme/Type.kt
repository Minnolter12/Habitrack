package com.minnolter.habitrack.ui.theme

import androidx.compose.material3.Typography

/**
 * The default M3 type scale. Habitract leans on font *weight* and *size*
 * choices at individual call sites (e.g. the Habit Detail hero, Section 25)
 * rather than a custom typeface, so the base scale is left at Material's
 * own defaults here.
 */
val HabitractTypography = Typography()
