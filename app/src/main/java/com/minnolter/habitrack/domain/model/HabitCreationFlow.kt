package com.minnolter.habitrack.domain.model

import java.util.Locale

enum class EstimationMode {
    ZERO_BASE,
    DIRECT_HOURS,
    HISTORICAL_CALCULATOR,
    MANUAL_SLIDER
}

enum class BreakUnit(val label: String) {
    YEARS("Years"),
    MONTHS("Months"),
    WEEKS("Weeks")
}

data class ScheduleExpectation(
    val sessionDurationHours: Float = 1.0f,
    val weeklyFrequencyDays: Int = 4
) {
    val sessionDurationMinutes: Int get() = (sessionDurationHours * 60f).toInt()
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
    val hasPracticedBefore: Boolean = false,
    val estimationMode: EstimationMode = EstimationMode.ZERO_BASE,
    val knownHours: Int = 0,
    val knownMinutes: Int = 0,
    val yearsPracticed: Int = 0,
    val breakValue: Int = 0,
    val breakUnit: BreakUnit = BreakUnit.MONTHS,
    val sessionsPerWeek: Int = 3,
    val hoursPerSession: Float = 1.0f,
    val consistencyFactor: Float = 0.85f,
    val manualOverrideHours: Float = 0f,
    val scheduleExpectation: ScheduleExpectation = ScheduleExpectation(),
    val colorHex: String = "#7C4DFF",
    val imageUrl: String? = null,
    val isCustomHabit: Boolean = false
) {
    val calculatedBaselineMinutes: Long
        get() {
            if (!hasPracticedBefore) return 0L
            val raw = if (estimationMode == EstimationMode.MANUAL_SLIDER) {
                (manualOverrideHours * 60f).toLong()
            } else {
                val grossMinutes = calculateGrossMinutes(
                    yearsPracticed = yearsPracticed.coerceAtMost(100),
                    sessionsPerWeek = sessionsPerWeek.coerceIn(0, 7),
                    hoursPerSession = hoursPerSession.coerceIn(0f, 24f),
                    consistencyFactor = consistencyFactor
                )
                val breakMinutes = calculateBreakMinutes(breakValue, breakUnit)
                (grossMinutes - breakMinutes).coerceAtLeast(0L)
            }
            // Absolute cap of 50,000 hours per card (3,000,000 minutes) to prevent overflow bugs
            return raw.coerceAtMost(50_000L * 60L)
        }

    val calculatedStage: ProgressionStage
        get() = ProgressionStage.fromMinutes(calculatedBaselineMinutes)
}

fun calculateGrossMinutes(
    yearsPracticed: Int,
    sessionsPerWeek: Int,
    hoursPerSession: Float,
    consistencyFactor: Float
): Long {
    val totalWeeks = yearsPracticed * 52f
    val totalSessions = totalWeeks * sessionsPerWeek
    val rawMinutes = totalSessions * (hoursPerSession * 60f)
    return (rawMinutes * consistencyFactor).toLong()
}

fun calculateBreakMinutes(value: Int, unit: BreakUnit): Long {
    if (value <= 0) return 0L
    return when (unit) {
        BreakUnit.YEARS -> (value * 52F * 7F * 60F).toLong()
        BreakUnit.MONTHS -> (value * 4.33F * 7F * 60F).toLong()
        BreakUnit.WEEKS -> (value * 7F * 60F).toLong()
    }
}
