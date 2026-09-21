package com.minnolter.habitrack.domain.model

import java.util.Calendar

enum class EstimationMode {
    ZERO_BASE,
    DIRECT_HOURS,
    HISTORICAL_CALCULATOR
}

data class ScheduleExpectation(
    val sessionDurationMinutes: Int = 45,
    val weeklyFrequencyDays: Int = 4
) {
    val weeklyMinutes: Long get() = (sessionDurationMinutes * weeklyFrequencyDays).toLong()

    fun calculateProjectedMonthsToStage(currentMinutes: Long, targetStage: ProgressionStage): Int? {
        val remainingMinutes = targetStage.minMinutes - currentMinutes
        if (remainingMinutes <= 0L) return 0
        if (weeklyMinutes <= 0L) return null

        val weeklyHours = weeklyMinutes.toDouble() / 60.0
        val remainingHours = remainingMinutes.toDouble() / 60.0
        val weeks = remainingHours / weeklyHours
        val months = (weeks / 4.33).toInt().coerceAtLeast(1)
        return months
    }

    fun calculateProjectedYearsToMaster(currentMinutes: Long): Float? {
        val remainingMinutes = ProgressionStage.MASTER.minMinutes - currentMinutes
        if (remainingMinutes <= 0L) return 0f
        if (weeklyMinutes <= 0L) return null

        val yearlyMinutes = weeklyMinutes * 52.0
        val years = remainingMinutes.toDouble() / yearlyMinutes
        return String.format("%.1f", years).toFloat()
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
    val consistencyFactor: Float = 0.85f, // 85% accounting for breaks/off-weeks
    val scheduleExpectation: ScheduleExpectation = ScheduleExpectation(),
    val colorHex: String = "#7C4DFF",
    val imageUrl: String? = null
) {
    val calculatedBaselineMinutes: Long
        get() = calculateBaselineMinutes(
            mode = estimationMode,
            knownHours = knownHours,
            knownMinutes = knownMinutes,
            yearsPracticed = yearsPracticed,
            monthsPracticed = monthsPracticed,
            sessionsPerWeek = sessionsPerWeek,
            minutesPerSession = minutesPerSession,
            consistencyFactor = consistencyFactor
        )

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
    }
}
