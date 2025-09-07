package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle

/**
 * Data Access Object for Cycle class.
 */
@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY position")
    fun getCycles(): List<Cycle>

    @Query("SELECT * FROM cycles ORDER BY position")
    fun observeCycles(): LiveData<List<Cycle>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCycle(cycle: Cycle): Long

    /**
     * Returns number of cycles updated (should always be 1).
     */
    @Update
    fun updateCycle(cycle: Cycle): Int

    @Query("UPDATE cycles SET name = :name WHERE id = :id")
    fun updateCycleName(id: Long, name: String)

    /**
     * Returns number of cycles updated (should always be 1).
     */
    @Delete
    fun deleteCycle(cycle: Cycle): Int

    @Query("SELECT * FROM cycles WHERE id = :cycleId")
    fun getCycleById(cycleId: Long): Cycle?

    /**
     * Used to workaround transaction limitation
     */
    @Insert
    fun insertCycleSync(cycle: Cycle): Long

    @Query("UPDATE cycles SET dateLastLogged = :date WHERE id = :id")
    fun updateTrainingDate(id: Long, date: Long)

    @Query("UPDATE cycles SET position = :position WHERE id = :id")
    fun updatePositionSync(id: Long, position: Int)
}