package com.minnolter.habitrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Persistent record for a single tracked habit/skill.
 *
 * Per the spec (Section 32), this entity intentionally does NOT carry a mutable
 * `totalMinutes`/`totalHours` field. Accumulated time is always derived by
 * aggregating [com.habitract.app.data.local.entity.PracticeSessionEntity] rows,
 * so historical data is never lost or allowed to drift out of sync.
 *
 * [description] was added in schema version 2 (Phase 5, `MIGRATION_1_2`) to
 * back the Habit Detail screen's Edit Habit dialog.
 */
@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["sortOrder"])
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "sortOrder")
    val sortOrder: Int,

    @ColumnInfo(name = "description", defaultValue = "NULL")
    val description: String? = null
)
