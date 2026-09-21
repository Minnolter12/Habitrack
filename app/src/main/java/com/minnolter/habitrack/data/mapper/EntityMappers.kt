package com.minnolter.habitrack.data.mapper

import com.minnolter.habitrack.data.local.entity.HabitEntity
import com.minnolter.habitrack.data.local.entity.PracticeSessionEntity
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.PracticeSession

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    createdAt = createdAt,
    imageUri = imageUri,
    colorHex = colorHex,
    sortOrder = sortOrder,
    description = description
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    imageUri = imageUri,
    colorHex = colorHex,
    sortOrder = sortOrder,
    description = description
)

fun PracticeSessionEntity.toDomain(): PracticeSession = PracticeSession(
    id = id,
    habitId = habitId,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    note = note
)

fun PracticeSession.toEntity(): PracticeSessionEntity = PracticeSessionEntity(
    id = id,
    habitId = habitId,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    note = note
)
