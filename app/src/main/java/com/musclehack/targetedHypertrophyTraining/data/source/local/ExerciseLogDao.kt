package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseLog

/**
 * Data Access Object for ExerciseLog
 */
@Dao
interface ExerciseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogSync(exerciseLog: ExerciseLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(exerciseLog: ExerciseLog)

    @Query("SELECT * FROM exerciseLogs")
    fun getExerciseLogs(): LiveData<List<ExerciseLog>>

    @Query("SELECT * FROM exerciseLogs")
    fun getAllExerciseLogs(): List<ExerciseLog>

    @Query("SELECT * FROM exerciseLogs WHERE day = :day AND setId IN (:setIds)")
    fun getExerciseLogs(setIds: List<Long>, day: Int): LiveData<List<ExerciseLog>>

    @Query("SELECT * FROM exerciseLogs WHERE day = :day AND :setId = setId")
    fun getExerciseLog(setId: Long, day: Int): ExerciseLog?

    @Query("SELECT * FROM exerciseLogs WHERE :setId = setId")
    fun getExerciseLogs(setId: Long): List<ExerciseLog>

    @Update
    fun update(exerciseLog: ExerciseLog)

    @Query("SELECT * FROM exerciseLogs WHERE day < :currentDay AND setId = :setId ORDER BY day DESC")
    fun getExerciseLogBeforeDay(currentDay: Int, setId: Long): List<ExerciseLog>

    @Query("SELECT * FROM exerciseLogs WHERE day < :currentDay AND setId = :setId ORDER BY day DESC")
    fun observeExerciseLogBeforeDay(currentDay: Int, setId: Long): LiveData<List<ExerciseLog>>

    // get all logs for a set up until this day
    @Query("SElECT * FROM exerciseLogs WHERE day <= :currentDay AND setID = :setId ORDER BY day DESC")
    fun getExerciseLogsBeforeAndIncludingDay(currentDay: Int, setId: Long): List<ExerciseLog>

    @Query("UPDATE exerciseLogs SET weight = :weight WHERE setId = :setId AND day = :day")
    fun updateWeight(setId: Long, day: Int, weight: Double?): Int

    @Query("UPDATE exerciseLogs SET reps = :reps WHERE setId = :setId AND day = :day")
    fun updateReps(setId: Long, day: Int, reps: Int?): Int

    @Query("UPDATE exerciseLogs SET skip = :skip WHERE setId = :setId AND day = :day")
    fun updateSkip(setId: Long, day: Int, skip: Boolean): Int

    @Query("UPDATE exerciseLogs SET hasNote = :hasNote WHERE setId = :setId AND day = :day")
    fun updateNoteStatus(setId: Long, day: Int, hasNote: Boolean): Int

    @Query("UPDATE exerciseLogs SET subName = :subName WHERE setId = :setId AND day = :day")
    fun updateSubstitutionSingleDay(subName: String?, setId: Long, day: Int)
}