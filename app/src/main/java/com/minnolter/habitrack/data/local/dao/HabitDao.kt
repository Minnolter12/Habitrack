package com.minnolter.habitrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.minnolter.habitrack.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun observeById(habitId: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getById(habitId: Long): HabitEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM habits")
    suspend fun getMaxSortOrder(): Int

    @Query("UPDATE habits SET sortOrder = :sortOrder WHERE id = :habitId")
    suspend fun updateSortOrder(habitId: Long, sortOrder: Int)

    /**
     * Applies a full reordering in one transaction so observers never see a
     * partially-reordered list between individual UPDATE statements.
     */
    @Transaction
    suspend fun reorder(habitIdsInOrder: List<Long>) {
        habitIdsInOrder.forEachIndexed { index, habitId ->
            updateSortOrder(habitId, index)
        }
    }
}
