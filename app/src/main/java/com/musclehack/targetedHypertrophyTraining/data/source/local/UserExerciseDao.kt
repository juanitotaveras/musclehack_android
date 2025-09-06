package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise

/**
 * Data Access Object for UserExercise
 */
@Dao
interface UserExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExercise(userExercise: UserExercise)

    @Query("SELECT * FROM userExercises")
    fun getAllUserExercises(): List<UserExercise>

    @Query("SELECT * FROM userExercises WHERE name = :name")
    fun getUserExercise(name: String): UserExercise?

    @Query("DELETE FROM userExercises WHERE name = :name")
    fun deleteUserExercise(name: String)
}