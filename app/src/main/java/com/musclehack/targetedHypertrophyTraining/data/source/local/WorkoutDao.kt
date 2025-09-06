package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout

/**
 * Data Access Object for Workout class.
 */
@Dao
interface WorkoutDao {
    @Query("SELECT * from workouts ORDER BY position")
    fun observeAllWorkouts(): LiveData<List<Workout>>

    @Query("SELECT * from workouts ORDER BY position")
    fun getAllWorkouts(): List<Workout>

    @Query("SELECT * from workouts WHERE cycleId = :cycleId ORDER BY position")
    fun getWorkouts(cycleId: Long): LiveData<List<Workout>>

    @Query("SELECT * from workouts WHERE cycleId = :cycleId ORDER BY position")
    fun getWorkoutsSync(cycleId: Long): List<Workout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutSync(workout: Workout): Long

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): Workout?

    @Update
    fun updateWorkout(workout: Workout): Int

    @Query("UPDATE workouts SET name = :name WHERE id = :id")
    fun updateWorkoutName(id: Long, name: String)

    /** To be called within a transaction. */
    @Update
    fun updateWorkoutSync(workout: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: Workout): Long

    @Delete
    fun deleteWorkout(workout: Workout)
}