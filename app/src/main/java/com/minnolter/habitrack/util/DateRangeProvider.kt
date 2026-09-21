package com.minnolter.habitrack.util

import com.minnolter.habitrack.domain.model.TimeRange
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Resolves a [TimeRange] filter into a concrete `[start, end]` epoch-millisecond
 * window, anchored to the device's current zone. Kept out of the DAO layer so
 * SQL stays a dumb range filter and all "what does 'this week' mean" logic
 * lives in one testable place.
 *
 * No DI framework is wired up yet (Phase 1 scope) — construct this directly
 * where needed, e.g. inside [com.habitract.app.data.repository.HabitractRepositoryImpl].
 */
class DateRangeProvider(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    data class Bounds(val startEpochMillis: Long, val endEpochMillis: Long)

    fun boundsFor(range: TimeRange, now: Instant = Instant.now()): Bounds {
        val zonedNow = ZonedDateTime.ofInstant(now, zoneId)

        val start: ZonedDateTime = when (range) {
            TimeRange.LIFETIME -> ZonedDateTime.ofInstant(Instant.EPOCH, zoneId)
            TimeRange.TODAY -> zonedNow.toLocalDate().atStartOfDay(zoneId)
            TimeRange.WEEK -> zonedNow.toLocalDate()
                .minusDays((zonedNow.dayOfWeek.value - 1).toLong()) // Monday start
                .atStartOfDay(zoneId)
            TimeRange.MONTH -> zonedNow.toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay(zoneId)
            TimeRange.YEAR -> zonedNow.toLocalDate()
                .withDayOfYear(1)
                .atStartOfDay(zoneId)
        }

        return Bounds(
            startEpochMillis = start.toInstant().toEpochMilli(),
            endEpochMillis = now.toEpochMilli()
        )
    }
}
