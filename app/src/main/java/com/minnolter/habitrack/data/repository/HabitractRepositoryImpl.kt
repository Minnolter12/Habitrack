package com.minnolter.habitrack.data.repository

import com.minnolter.habitrack.data.local.dao.HabitDao
import com.minnolter.habitrack.data.local.dao.PracticeSessionDao
import com.minnolter.habitrack.data.mapper.toDomain
import com.minnolter.habitrack.data.mapper.toEntity
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.PracticeSession
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.util.DateRangeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [HabitractRepository]. Translates between
 * [TimeRange] and concrete epoch-millis windows via [dateRangeProvider], and
 * between Room entities and domain models via the mappers in
 * `com.habitract.app.data.mapper`.
 */
class HabitractRepositoryImpl(
    private val habitDao: HabitDao,
    private val practiceSessionDao: PracticeSessionDao,
    private val dateRangeProvider: DateRangeProvider = DateRangeProvider()
) : HabitractRepository {

    // ---- Habits ----

    override fun observeHabits(): Flow<List<Habit>> =
        habitDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeHabit(habitId: Long): Flow<Habit?> =
        habitDao.observeById(habitId).map { it?.toDomain() }

    override suspend fun addHabit(habit: Habit): Long {
        val nextSortOrder = habitDao.getMaxSortOrder() + 1
        return habitDao.insert(habit.copy(sortOrder = nextSortOrder).toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.delete(habit.toEntity())
    }

    override suspend fun reorderHabits(habitIdsInOrder: List<Long>) {
        habitDao.reorder(habitIdsInOrder)
    }

    // ---- Practice sessions ----

    override fun observeSessions(habitId: Long): Flow<List<PracticeSession>> =
        practiceSessionDao.observeSessionsForHabit(habitId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeRecentSessions(habitId: Long, limit: Int): Flow<List<PracticeSession>> =
        practiceSessionDao.observeRecentSessionsForHabit(habitId, limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun logSession(session: PracticeSession): Long =
        practiceSessionDao.insert(session.toEntity())

    override suspend fun updateSession(session: PracticeSession) {
        practiceSessionDao.update(session.toEntity())
    }

    override suspend fun deleteSession(session: PracticeSession) {
        practiceSessionDao.delete(session.toEntity())
    }

    override suspend fun deleteSessionById(sessionId: Long) {
        practiceSessionDao.deleteById(sessionId)
    }

    // ---- Per-habit aggregates ----

    override fun observeMinutes(habitId: Long, range: TimeRange): Flow<Int> {
        if (range == TimeRange.LIFETIME) {
            return practiceSessionDao.observeLifetimeMinutesForHabit(habitId)
        }
        val bounds = dateRangeProvider.boundsFor(range)
        return practiceSessionDao.observeMinutesForHabitInRange(
            habitId = habitId,
            startEpochMillis = bounds.startEpochMillis,
            endEpochMillis = bounds.endEpochMillis
        )
    }

    override fun observeSessionCount(habitId: Long): Flow<Int> =
        practiceSessionDao.observeSessionCountForHabit(habitId)

    override fun observeLongestSession(habitId: Long): Flow<PracticeSession?> =
        practiceSessionDao.observeLongestSessionForHabit(habitId).map { it?.toDomain() }

    // ---- Cross-habit (dashboard) aggregates ----

    override suspend fun resetAllData() {
        practiceSessionDao.deleteAll()
        habitDao.deleteAll()
    }

    override fun observeTotalMinutes(range: TimeRange): Flow<Int> {
        if (range == TimeRange.LIFETIME) {
            return practiceSessionDao.observeLifetimeMinutesTotal()
        }
        val bounds = dateRangeProvider.boundsFor(range)
        return practiceSessionDao.observeMinutesTotalInRange(
            startEpochMillis = bounds.startEpochMillis,
            endEpochMillis = bounds.endEpochMillis
        )
    }

    override fun observeTotalSessionCount(): Flow<Int> =
        practiceSessionDao.observeSessionCountTotal()

    override fun observeLongestSessionOverall(): Flow<PracticeSession?> =
        practiceSessionDao.observeLongestSessionOverall().map { it?.toDomain() }
}
