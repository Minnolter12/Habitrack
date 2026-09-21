package com.minnolter.habitrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A single logged unit of practice for a habit.
 *
 * Per Section 15 of the spec, [durationMinutes] is the source of truth for
 * elapsed time and must never be discarded or approximated — all lifetime,
 * daily, weekly, monthly, and yearly totals are derived by summing these rows.
 *
 * Deleting a [HabitEntity] cascades to delete its sessions, since orphaned
 * sessions would violate referential integrity and skew global dashboard
 * aggregates.
 *
 * [note] was added in schema version 2 (Phase 5, `MIGRATION_1_2`) to back
 * the manual session log/edit dialog on the Habit Detail screen.
 */
@Entity(
    tableName = "practice_sessions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habit_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["habit_id", "timestamp"])
    ]
)
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Instant,

    @ColumnInfo(name = "note", defaultValue = "NULL")
    val note: String? = null
)
