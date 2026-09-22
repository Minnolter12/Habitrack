package com.minnolter.habitrack.domain.model

import java.util.Locale
import kotlin.math.roundToLong

enum class EstimationMode {
    ZERO_BASE,
    DIRECT_HOURS,
    HISTORICAL_CALCULATOR,
    MANUAL_SLIDER
}

data class ScheduleExpectation(
    val sessionDurationMinutes: Int = 60,
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
    val hasPracticedBefore: Boolean = false,
    val estimationMode: EstimationMode = EstimationMode.ZERO_BASE,
    
    // Q1: Total Timespan Practicing
    val timespanYears: Int = 0,
    val timespanMonths: Int = 0, // 0..11
    val timespanWeeks: Int = 0,  // 0..4

    // Q2: Off-Time & Breaks
    val offTimeYears: Int = 0,
    val offTimeMonths: Int = 0,  // 0..11
    val offTimeWeeks: Int = 0,   // 0..4

    // Q3 & Q4: Cadence & Consistency
    val sessionsPerWeek: Int = 3,
    val minutesPerSession: Int = 60,
    val consistencyPercentage: Int = 85, // 50..100

    // Fine-Tuning adjustment relative to calculatedBaseHours (-100..+100)
    val manualAdjustmentHours: Long = 0L,

    val scheduleExpectation: ScheduleExpectation = ScheduleExpectation(),
    val colorHex: String = "#7C4DFF",
    val imageUrl: String? = null,
    val isCustomHabit: Boolean = false
) {
    val grossWeeks: Double get() = (timespanYears * 52.0) + (timespanMonths * 4.33) + timespanWeeks.toDouble()
    val breakWeeks: Double get() = (offTimeYears * 52.0) + (offTimeMonths * 4.33) + offTimeWeeks.toDouble()
    val netActiveWeeks: Double get() = (grossWeeks - breakWeeks).coerceAtLeast(0.0)

    val weeklyHours: Double get() = sessionsPerWeek * (minutesPerSession / 60.0)
    val rawCalculatedHours: Double get() = netActiveWeeks * weeklyHours * (consistencyPercentage / 100.0)
    val calculatedBaseHours: Long get() = rawCalculatedHours.roundToLong()

    val minAllowedHours: Long get() = (calculatedBaseHours - 100L).coerceAtLeast(0L)
    val maxAllowedHours: Long get() = calculatedBaseHours + 100L

    val fineTunedBaseHours: Long
        get() = (calculatedBaseHours + manualAdjustmentHours).coerceIn(minAllowedHours, maxAllowedHours)

    val calculatedBaselineMinutes: Long
        get() {
            if (!hasPracticedBefore) return 0L
            val hours = fineTunedBaseHours
            // Cap at 50,000 hours per card (3,000,000 minutes) to prevent overflow bugs
            return (hours * 60L).coerceAtMost(50_000L * 60L)
        }

    val calculatedStage: ProgressionStage
        get() = ProgressionStage.fromMinutes(calculatedBaselineMinutes)

    /**
     * Input validation:
     * - Timespan must have at least one non-zero value
     * - Off-time weeks cannot exceed gross active weeks
     * - Cadence must have non-zero sessions and duration
     */
    val isTimespanValid: Boolean get() = (timespanYears > 0 || timespanMonths > 0 || timespanWeeks > 0)
    val isOffTimeValid: Boolean get() = breakWeeks <= grossWeeks
    val isCadenceValid: Boolean get() = (sessionsPerWeek > 0 && minutesPerSession > 0)
}
