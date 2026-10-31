package com.minnolter.habitrack.util

import com.minnolter.habitrack.domain.model.TimeRange
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateRangeProvider(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    data class Bounds(val startEpochMillis: Long, val endEpochMillis: Long)

    fun boundsFor(range: TimeRange, now: Instant = Instant.now(), offset: Int = 0): Bounds {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)

        return when (range) {
            TimeRange.LIFETIME -> Bounds(
                startEpochMillis = Instant.EPOCH.toEpochMilli(),
                endEpochMillis = now.toEpochMilli()
            )

            TimeRange.TODAY -> {
                val targetDate = zonedNow.toLocalDate().plusDays(offset.toLong())
                val start = targetDate.atStartOfDay(zoneId)
                val end = targetDate.plusDays(1).atStartOfDay(zoneId).minusNanos(1)
                Bounds(
                    startEpochMillis = start.toInstant().toEpochMilli(),
                    endEpochMillis = if (offset == 0) now.toEpochMilli() else end.toInstant().toEpochMilli()
                )
            }

            TimeRange.WEEK -> {
                val currentMonday = zonedNow.toLocalDate().minusDays((zonedNow.dayOfWeek.value - 1).toLong())
                val targetMonday = currentMonday.plusWeeks(offset.toLong())
                val start = targetMonday.atStartOfDay(zoneId)
                val end = targetMonday.plusWeeks(1).atStartOfDay(zoneId).minusNanos(1)
                Bounds(
                    startEpochMillis = start.toInstant().toEpochMilli(),
                    endEpochMillis = if (offset == 0) now.toEpochMilli() else end.toInstant().toEpochMilli()
                )
            }

            TimeRange.MONTH -> {
                val currentMonthStart = zonedNow.toLocalDate().withDayOfMonth(1)
                val targetMonthStart = currentMonthStart.plusMonths(offset.toLong())
                val start = targetMonthStart.atStartOfDay(zoneId)
                val end = targetMonthStart.plusMonths(1).atStartOfDay(zoneId).minusNanos(1)
                Bounds(
                    startEpochMillis = start.toInstant().toEpochMilli(),
                    endEpochMillis = if (offset == 0) now.toEpochMilli() else end.toInstant().toEpochMilli()
                )
            }

            TimeRange.YEAR -> {
                val currentYearStart = zonedNow.toLocalDate().withDayOfYear(1)
                val targetYearStart = currentYearStart.plusYears(offset.toLong())
                val start = targetYearStart.atStartOfDay(zoneId)
                val end = targetYearStart.plusYears(1).atStartOfDay(zoneId).minusNanos(1)
                Bounds(
                    startEpochMillis = start.toInstant().toEpochMilli(),
                    endEpochMillis = if (offset == 0) now.toEpochMilli() else end.toInstant().toEpochMilli()
                )
            }
        }
    }

    fun periodLabel(range: TimeRange, now: Instant = Instant.now(), offset: Int = 0): String {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)
        return when (range) {
            TimeRange.LIFETIME -> "Lifetime"
            TimeRange.TODAY -> {
                val target = zonedNow.toLocalDate().plusDays(offset.toLong())
                if (offset == 0) "Today"
                else target.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            }
            TimeRange.WEEK -> {
                val currentMonday = zonedNow.toLocalDate().minusDays((zonedNow.dayOfWeek.value - 1).toLong())
                val targetMonday = currentMonday.plusWeeks(offset.toLong())
                val targetSunday = targetMonday.plusDays(6)
                "${targetMonday.format(DateTimeFormatter.ofPattern("MMM d"))} - ${targetSunday.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            }
            TimeRange.MONTH -> {
                val targetMonth = zonedNow.toLocalDate().withDayOfMonth(1).plusMonths(offset.toLong())
                targetMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
            TimeRange.YEAR -> {
                val targetYear = zonedNow.toLocalDate().withDayOfYear(1).plusYears(offset.toLong())
                targetYear.format(DateTimeFormatter.ofPattern("yyyy"))
            }
        }
    }
}
