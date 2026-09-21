package com.minnolter.habitrack.util

import java.text.NumberFormat
import java.util.Locale

/**
 * The Section 15 **card/dashboard** display rule: exact minutes below one
 * hour, exact hours-and-minutes below 100 hours, and locale-aware whole
 * hours (with thousands separators) at and above 100 hours. Used by
 * [com.habitract.app.ui.components.HabitCard] and
 * [com.habitract.app.ui.screens.home.components.DashboardSummaryHeader].
 *
 * The underlying stored minute value is never rounded — only this label is.
 */
fun formatAccumulatedDuration(totalMinutes: Long): String {
    if (totalMinutes < 60L) return "${totalMinutes}m"

    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return if (hours < 100L) {
        if (minutes == 0L) "${hours}h" else "${hours}h ${minutes}m"
    } else {
        val formattedHours = NumberFormat.getIntegerInstance(Locale.getDefault()).format(hours)
        "${formattedHours}h"
    }
}

/**
 * The Section 15 **detail-screen** display rule: "the detailed statistics
 * screen should still show 101h 45m" — exact hours *and* minutes, always,
 * with no 100-hour simplification threshold. Used by the Habit Detail
 * screen's hero header and session history rows.
 */
fun formatExactDuration(totalMinutes: Long): String {
    if (totalMinutes < 60L) return "${totalMinutes}m"

    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    val formattedHours = NumberFormat.getIntegerInstance(Locale.getDefault()).format(hours)

    return if (minutes == 0L) "${formattedHours}h" else "${formattedHours}h ${minutes}m"
}
