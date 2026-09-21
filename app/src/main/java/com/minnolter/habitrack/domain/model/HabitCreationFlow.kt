package com.minnolter.habitrack.domain.model

import java.util.Locale

enum class EstimationMode {
    ZERO_BASE,
    DIRECT_HOURS,
    HISTORICAL_CALCULATOR,
    MANUAL_SLIDER
}

data class ScheduleExpectation(
    val sessionDurationMinutes: Int = 45,
    val weeklyFrequencyDays: Int = 4
) {
    val weeklyMinutes: Long get() = (sessionDurationMinutes * weeklyFrequencyDays).toLong()

    fun calculateProjectedYearsToMaster(currentMinutes: Long): Float? {
        val remainingMinutes = ProgressionStage.MASTER.minMinutes - currentMinutes
        if (remainingMinutes <= 0L) return 0f
        if (weeklyMinutes <= 0L) return null

        val yearlyMinutes = weeklyMinutes * 52.0
        val years = remainingMinutes.toDouble() / yearlyMinutes
        return String.format(Locale.US, "%.1f", years).toFloatOrNull() ?: 0f
    }
}

data class HabitCreationDraft(
    val habitName: String = "",
    val category: HabitCategory = HabitCategory.MUSIC,
    val estimationMode: EstimationMode = EstimationMode.ZERO_BASE,
    val knownHours: Int = 0,
    val knownMinutes: Int = 0,
    val yearsPracticed: Int = 0,
    val monthsPracticed: Int = 0,
    val sessionsPerWeek: Int = 3,
    val minutesPerSession: Int = 45,
    val consistencyFactor: Float = 0.85f,
    val manualOverrideHours: Float = 0f,
    val scheduleExpectation: ScheduleExpectation = ScheduleExpectation(),
    val colorHex: String = "#7C4DFF",
    val imageUrl: String? = null,
    val isCustomHabit: Boolean = false
) {
    val calculatedBaselineMinutes: Long
        get() {
            val raw = if (estimationMode == EstimationMode.MANUAL_SLIDER) {
                (manualOverrideHours * 60f).toLong()
            } else {
                calculateBaselineMinutes(
                    mode = estimationMode,
                    knownHours = knownHours,
                    knownMinutes = knownMinutes,
                    yearsPracticed = yearsPracticed.coerceAtMost(100),
                    monthsPracticed = monthsPracticed.coerceIn(0, 11),
                    sessionsPerWeek = sessionsPerWeek.coerceIn(0, 7),
                    minutesPerSession = minutesPerSession.coerceIn(0, 1440),
                    consistencyFactor = consistencyFactor
                )
            }
            // Absolute cap of 50,000 hours per card (3,000,000 minutes) to prevent overflow bugs
            return raw.coerceAtMost(50_000L * 60L)
        }

    val calculatedStage: ProgressionStage
        get() = ProgressionStage.fromMinutes(calculatedBaselineMinutes)
}

fun calculateBaselineMinutes(
    mode: EstimationMode,
    knownHours: Int = 0,
    knownMinutes: Int = 0,
    yearsPracticed: Int = 0,
    monthsPracticed: Int = 0,
    sessionsPerWeek: Int = 0,
    minutesPerSession: Int = 0,
    consistencyFactor: Float = 0.85f
): Long {
    return when (mode) {
        EstimationMode.ZERO_BASE -> 0L
        EstimationMode.DIRECT_HOURS -> (knownHours * 60L + knownMinutes).coerceAtLeast(0L)
        EstimationMode.HISTORICAL_CALCULATOR -> {
            val totalMonths = (yearsPracticed * 12) + monthsPracticed
            val totalWeeks = totalMonths * 4.33f
            val totalEstimatedSessions = totalWeeks * sessionsPerWeek
            val rawMinutes = totalEstimatedSessions * minutesPerSession
            (rawMinutes * consistencyFactor).toLong().coerceAtLeast(0L)
        }
        EstimationMode.MANUAL_SLIDER -> 0L
    }
}
