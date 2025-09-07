package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.exerciseBank.DefaultExercises
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise

interface ExerciseBankRepository {
    suspend fun getDefaultExercises(): DefaultExercises

    suspend fun isExerciseNameInBank(exerciseName: String): Boolean

    suspend fun addExerciseNameToBank(exerciseName: String): Result<String>

    suspend fun getUserExercises(): Result<List<UserExercise>>

    suspend fun deleteUserExercise(exerciseName: String): Result<Nothing?>
}