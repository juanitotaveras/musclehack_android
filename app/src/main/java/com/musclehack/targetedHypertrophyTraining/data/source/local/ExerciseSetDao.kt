package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet

/**
 * Data Access Object for Set
 */
@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM sets WHERE cycleId = :cycleId AND workoutId = :workoutId ORDER BY position")
    fun getExerciseSets(cycleId: Long, workoutId: Long): LiveData<List<ExerciseSet>>

    @Query("SELECT * FROM sets WHERE cycleId = :cycleId AND workoutId = :workoutId ORDER BY position")
    fun getExerciseSetsSync(cycleId: Long, workoutId: Long): List<ExerciseSet>

    @Query("SELECT * FROM sets ORDER BY position")
    fun getAllExerciseSets(): List<ExerciseSet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSet(exerciseSet: ExerciseSet)

    /** Not using suspend because we are running this in a transaction */
    @Update
    fun updateSetSync(exerciseSet: ExerciseSet)

    @Update
    fun updateExerciseSet(exerciseSet: ExerciseSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSetSync(exerciseSet: ExerciseSet): Long

    @Delete
    fun deleteExerciseSet(exerciseSet: ExerciseSet)

    @Query("UPDATE sets SET lowerReps = :minReps WHERE id = :id")
    fun updateMinReps(id: Long, minReps: Int)

    @Query("UPDATE sets SET higherReps = :maxReps WHERE id = :id")
    fun updateMaxReps(id: Long, maxReps: Int)

    @Query("UPDATE sets SET restTime = :restTimeSeconds WHERE id = :id")
    fun updateRestTime(id: Long, restTimeSeconds: Int)

    @Query("UPDATE sets SET position = :position WHERE id = :id")
    fun updatePosition(id: Long, position: Int)

    @Query("UPDATE sets SET exerciseName = :name WHERE id = :id")
    fun updateExerciseName(id: Long, name: String)

    @Query("SELECT * FROM sets WHERE id = :setId")
    fun getExerciseSet(setId: Long): ExerciseSet?

    @Query("SELECT * FROM sets WHERE cycleId = :cycleId AND workoutId = :workoutId AND position = :position")
    fun getExerciseSet(cycleId: Long, workoutId: Long, position: Int): ExerciseSet?
}