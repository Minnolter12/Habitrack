package com.minnolter.habitrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minnolter.habitrack.data.local.entity.PracticeSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeSessionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: PracticeSessionEntity): Long

    /** Added in Phase 5 for the Habit Detail screen's session edit dialog. */
    @Update
    suspend fun update(session: PracticeSessionEntity)

    @Delete
    suspend fun delete(session: PracticeSessionEntity)

    @Query("DELETE FROM practice_sessions")
    suspend fun deleteAll()

    /**
     * Added in Phase 5: swipe-to-delete on the Habit Detail screen's session
     * history only has a session id on hand, not a full loaded entity.
     */
    @Query("DELETE FROM practice_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)

    @Query("SELECT * FROM practice_sessions WHERE habit_id = :habitId ORDER BY timestamp DESC")
    fun observeSessionsForHabit(habitId: Long): Flow<List<PracticeSessionEntity>>

    @Query(
        """
        SELECT * FROM practice_sessions
        WHERE habit_id = :habitId
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun observeRecentSessionsForHabit(habitId: Long, limit: Int): Flow<List<PracticeSessionEntity>>

    // ---- Per-habit aggregates ----

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM practice_sessions WHERE habit_id = :habitId")
    fun observeLifetimeMinutesForHabit(habitId: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(duration_minutes), 0) FROM practice_sessions
        WHERE habit_id = :habitId AND timestamp BETWEEN :startEpochMillis AND :endEpochMillis
        """
    )
    fun observeMinutesForHabitInRange(
        habitId: Long,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Flow<Int>

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE habit_id = :habitId")
    fun observeSessionCountForHabit(habitId: Long): Flow<Int>

    @Query(
        """
        SELECT * FROM practice_sessions
        WHERE habit_id = :habitId
        ORDER BY duration_minutes DESC
        LIMIT 1
        """
    )
    fun observeLongestSessionForHabit(habitId: Long): Flow<PracticeSessionEntity?>

    // ---- Cross-habit (dashboard) aggregates ----

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM practice_sessions")
    fun observeLifetimeMinutesTotal(): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(duration_minutes), 0) FROM practice_sessions
        WHERE timestamp BETWEEN :startEpochMillis AND :endEpochMillis
        """
    )
    fun observeMinutesTotalInRange(startEpochMillis: Long, endEpochMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM practice_sessions")
    fun observeSessionCountTotal(): Flow<Int>

    @Query("SELECT * FROM practice_sessions ORDER BY duration_minutes DESC LIMIT 1")
    fun observeLongestSessionOverall(): Flow<PracticeSessionEntity?>
}
