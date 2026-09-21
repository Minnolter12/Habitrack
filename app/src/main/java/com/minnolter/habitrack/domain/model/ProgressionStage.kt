package com.minnolter.habitrack.domain.model

import kotlin.math.sqrt

/**
 * The 14-stage lifetime-investment progression (Sections 20–22).
 *
 * Deliberately data-driven and centralized: thresholds and colors live here,
 * not scattered across UI code, so the whole progression can be re-tuned in
 * one place (Section 21 explicitly calls this out).
 *
 * [minMinutes] is the inclusive lower bound, in minutes, at which a habit
 * enters this stage. Ordered ascending; [Master] has no upper bound.
 *
 * Colors follow the Section 22 story — purple/violet early, moving through
 * blue and teal, into green, richer indigo/plum tones for Advanced/Expert,
 * and an elegant gold for the Mastery+ tier — while staying deliberately
 * unsaturated so they sit comfortably on Material 3 surfaces in both light
 * and dark themes. These are starting values per the spec and can be
 * re-tuned without touching any call site.
 */
enum class ProgressionStage(
    val displayName: String,
    val minHours: Int,
    val colorHex: String
) {
    JUST_STARTED("Just Started", minHours = 0, colorHex = "#B8B4E8"),
    BEGINNER("Beginner", minHours = 1, colorHex = "#A296E0"),
    NOVICE("Novice", minHours = 10, colorHex = "#8C7FD6"),
    DEVELOPING("Developing", minHours = 30, colorHex = "#7B8FE0"),
    PRACTICING("Practicing", minHours = 60, colorHex = "#5B9BD5"),
    COMPETENT("Competent", minHours = 100, colorHex = "#4AAFA5"),
    SKILLED("Skilled", minHours = 250, colorHex = "#52B788"),
    PROFICIENT("Proficient", minHours = 500, colorHex = "#3A9D6F"),
    ADVANCED("Advanced", minHours = 1_000, colorHex = "#6C63B5"),
    EXPERT("Expert", minHours = 2_000, colorHex = "#5C4B7A"),
    MASTERY("Mastery", minHours = 3_500, colorHex = "#B8912E"),
    VIRTUOSO("Virtuoso", minHours = 5_000, colorHex = "#C9A227"),
    ELITE("Elite", minHours = 7_500, colorHex = "#CBA135"),
    MASTER("Master", minHours = 10_000, colorHex = "#E5C158");

    val minMinutes: Long get() = minHours * MINUTES_PER_HOUR

    companion object {
        private const val MINUTES_PER_HOUR = 60L
        private const val MASTER_THRESHOLD_MINUTES = 10_000L * MINUTES_PER_HOUR

        /** Stages sorted from highest threshold to lowest, computed once. */
        private val descendingByThreshold = entries.sortedByDescending { it.minMinutes }

        /**
         * Resolves the progression stage a habit is currently in, given its
         * total accumulated lifetime minutes.
         */
        fun fromMinutes(totalMinutes: Long): ProgressionStage {
            val sanitizedMinutes = totalMinutes.coerceAtLeast(0L)
            return descendingByThreshold.firstOrNull { sanitizedMinutes >= it.minMinutes } ?: JUST_STARTED
        }

        /**
         * The true, linear fraction of the way to the 10,000-hour Master
         * threshold, clamped to [0, 1]. This is the number that must be shown
         * whenever an *honest* percentage is needed (Section 20) — never the
         * visual-only curve below.
         */
        fun calculateActualProgress(totalMinutes: Long): Float {
            if (totalMinutes <= 0L) return 0f
            return (totalMinutes.toFloat() / MASTER_THRESHOLD_MINUTES.toFloat()).coerceIn(0f, 1f)
        }

        /**
         * A nonlinear remapping of [calculateActualProgress] used purely for
         * the on-card fill (jelly visualization, progress bars).
         *
         * A straight 0–10,000h linear bar makes the first 100 hours
         * (1% of the total) nearly invisible, which contradicts the product
         * goal of early progress feeling meaningful (Section 20). Taking the
         * square root of the true fraction compresses the visual scale so
         * early minutes register clearly while staying monotonic and
         * strictly bounded to [0, 1] — it never overstates the real number,
         * it just makes low values easier to see. The exact percentage
         * should always be sourced from [calculateActualProgress] wherever
         * it's displayed as text.
         *
         * Reference points: 100h -> ~10% visual fill, 1,000h -> ~41%,
         * 2,500h -> ~50%, 10,000h -> 100%.
         */
        fun calculateVisualProgress(totalMinutes: Long): Float {
            val actual = calculateActualProgress(totalMinutes)
            return sqrt(actual).coerceIn(0f, 1f)
        }
    }
}
