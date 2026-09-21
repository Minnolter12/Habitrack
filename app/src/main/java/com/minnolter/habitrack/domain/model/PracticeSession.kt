package com.minnolter.habitrack.domain.model

import java.time.Instant

/**
 * A single logged unit of practice. [durationMinutes] is the atomic, honest
 * record of time invested (Section 15) — never rounded or discarded.
 *
 * [note] was added in Phase 5 to back the manual session log/edit dialog on
 * the Habit Detail screen; it did not exist in the original Phase 1 schema.
 * See `HabitractDatabase`'s `MIGRATION_1_2` for the corresponding column.
 */
data class PracticeSession(
    val id: Long,
    val habitId: Long,
    val durationMinutes: Int,
    val timestamp: Instant,
    val note: String? = null
)
