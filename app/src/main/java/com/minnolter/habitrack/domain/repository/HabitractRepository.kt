package com.minnolter.habitrack.domain.repository

import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.PracticeSession
import com.minnolter.habitrack.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth boundary between the domain layer and persistence.
 * Every read is exposed as a reactive [Flow] so ViewModels can collect
 * aggregates directly into UI state without manual re-querying (Section 31).
 */
interface HabitractRepository {

    // ---- Habits ----

    fun observeHabits(): Flow<List<Habit>>

    fun observeHabit(habitId: Long): Flow<Habit?>

    /** Inserts [habit], assigning it the next available sort order, and returns its new id. */
    suspend fun addHabit(habit: Habit): Long

    suspend fun updateHabit(habit: Habit)

    suspend fun deleteHabit(habit: Habit)

    /** Persists a full reordering, given habit ids in their new display order. */
    suspend fun reorderHabits(habitIdsInOrder: List<Long>)

    // ---- Practice sessions ----

    fun observeSessions(habitId: Long): Flow<List<PracticeSession>>

    fun observeRecentSessions(habitId: Long, limit: Int): Flow<List<PracticeSession>>

    /** Logs a new session and returns its new id. */
    suspend fun logSession(session: PracticeSession): Long

    /**
     * Overwrites an existing session's fields (Phase 5: the Habit Detail
     * screen's manual session edit dialog). [session] must carry the id of
     * the row being edited.
     */
    suspend fun updateSession(session: PracticeSession)

    suspend fun deleteSession(session: PracticeSession)

    /**
     * Deletes a session by id alone (Phase 5: swipe-to-delete on the Habit
     * Detail screen only has the id of the rendered row on hand).
     */
    suspend fun deleteSessionById(sessionId: Long)

    // ---- Per-habit aggregates ----

    fun observeMinutes(habitId: Long, range: TimeRange): Flow<Int>

    fun observeSessionCount(habitId: Long): Flow<Int>

    fun observeLongestSession(habitId: Long): Flow<PracticeSession?>

    // ---- Cross-habit (dashboard) aggregates ----

    fun observeTotalMinutes(range: TimeRange): Flow<Int>

    fun observeTotalSessionCount(): Flow<Int>

    fun observeLongestSessionOverall(): Flow<PracticeSession?>
}
