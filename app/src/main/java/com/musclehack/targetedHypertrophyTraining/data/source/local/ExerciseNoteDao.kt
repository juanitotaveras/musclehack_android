package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseNote

/** Data Access Object for ExerciseNote
 */
@Dao
interface ExerciseNoteDao {
    @Query("SELECT * FROM exerciseNotes WHERE setId = :setId AND day = :day")
    fun getExerciseNote(setId: Long, day: Int): ExerciseNote?

    @Query("SELECT * FROM exerciseNotes WHERE setId = :setId")
    fun getExerciseNotes(setId: Long): List<ExerciseNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExerciseNote(exerciseNote: ExerciseNote)

    @Query("SELECT * FROM exerciseNotes")
    fun getAllExerciseNotes(): List<ExerciseNote>

    @Update
    fun updateExerciseNote(exerciseNote: ExerciseNote)
}