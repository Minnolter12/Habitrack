package com.minnolter.habitrack.util

import androidx.compose.ui.graphics.Color
import com.minnolter.habitrack.domain.model.ProgressionStage

/**
 * Calculates a dynamic 100+ hue color shade for a habit based on its total logged
 * lifetime minutes and base progression stage.
 *
 * Every 10 hours (600 minutes) logged, the hue shade shifts incrementally across
 * 100+ smooth spectrum shades (including light yellow, warm gold, emerald, cyan,
 * royal blue, indigo, magenta, and rose gold), ensuring the user's jelly visualization
 * is constantly evolving rather than remaining stagnant for thousands of hours.
 */
fun getDynamicHabitColor(
    baseStage: ProgressionStage,
    lifetimeMinutes: Long,
    customColorHex: String? = null
): Color {
    val tenHourBlocks = (lifetimeMinutes / 600L).toInt()

    // 100+ distinct hue shifts (7.2 degrees per 10-hour block = full 360 deg cycle)
    val baseHue = getBaseStageHue(baseStage, customColorHex)
    val shiftedHue = (baseHue + (tenHourBlocks * 7.2f)) % 360f

    return hslToColor(
        hue = shiftedHue,
        saturation = if (baseStage == ProgressionStage.MASTER) 0.65f else 0.55f,
        lightness = if (baseStage == ProgressionStage.MASTER) 0.65f else 0.58f
    )
}

private fun getBaseStageHue(stage: ProgressionStage, customColorHex: String?): Float {
    if (!customColorHex.isNullOrBlank()) {
        try {
            val parsedColor = android.graphics.Color.parseColor(customColorHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(parsedColor, hsv)
            return hsv[0]
        } catch (_: Exception) {}
    }

    return when (stage) {
        ProgressionStage.JUST_STARTED -> 245f // Pastel Violet
        ProgressionStage.BEGINNER -> 255f     // Soft Purple
        ProgressionStage.NOVICE -> 265f       // Indigo
        ProgressionStage.DEVELOPING -> 220f   // Soft Blue
        ProgressionStage.PRACTICING -> 205f   // Sky Blue
        ProgressionStage.COMPETENT -> 175f    // Cyan/Teal
        ProgressionStage.SKILLED -> 150f      // Emerald
        ProgressionStage.PROFICIENT -> 135f   // Forest Green
        ProgressionStage.ADVANCED -> 275f     // Deep Purple
        ProgressionStage.EXPERT -> 300f       // Plum/Magenta
        ProgressionStage.MASTERY -> 45f       // Warm Amber
        ProgressionStage.VIRTUOSO -> 50f      // Light Yellow Gold
        ProgressionStage.ELITE -> 48f         // Pure Gold
        ProgressionStage.MASTER -> 52f        // Prestigious Light Yellow / Champagne Gold
    }
}

/**
 * Helper to convert HSL (Hue 0..360, Saturation 0..1, Lightness 0..1) to Compose Color.
 */
private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - Math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - Math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r1, g1, b1) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}
