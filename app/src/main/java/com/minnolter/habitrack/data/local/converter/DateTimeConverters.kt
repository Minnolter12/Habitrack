package com.minnolter.habitrack.data.local.converter

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Stores all timestamps as epoch millisecond [Long] values in SQLite while
 * exposing [Instant] everywhere in entities/DAOs. Keeping this conversion
 * centralized means every date-bearing column in the schema behaves
 * identically and stays timezone-agnostic at the storage layer — range
 * filtering (today/week/month/year) is resolved to concrete epoch-millis
 * boundaries by the caller before hitting the database.
 */
class DateTimeConverters {

    @TypeConverter
    fun fromEpochMillis(epochMillis: Long?): Instant? {
        return epochMillis?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
