package com.minnolter.habitrack.domain.model

import java.time.Instant

/**
 * Domain-level representation of a tracked habit/skill.
 *
 * Deliberately holds no time totals — those are always queried through
 * [com.habitract.app.domain.repository.HabitractRepository] as derived,
 * reactive aggregates over [PracticeSession] rows (Section 32).
 *
 * [description] was added in Phase 5 to back the Habit Detail screen's Edit
 * Habit dialog; it did not exist in the original Phase 1 schema (Section 32
 * lists only id/name/createdAt/imageUri/color/sortOrder). See
 * `HabitractDatabase`'s `MIGRATION_1_2` for the corresponding column.
 */
data class Habit(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val imageUri: String?,
    val colorHex: String,
    val sortOrder: Int,
    val description: String? = null
)
