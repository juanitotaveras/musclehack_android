package com.musclehack.targetedHypertrophyTraining.data.repository

import android.content.Context
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.TrackerLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.TrackerDataSource
import com.musclehack.targetedHypertrophyTraining.exerciseBank.DefaultExercises
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import javax.inject.Inject

class DefaultExerciseBankRepository @Inject constructor(
    private val appContext: Context,
    @TrackerLocalDataSource private val trackerLocalDataSource: TrackerDataSource
) : ExerciseBankRepository {

    override suspend fun getDefaultExercises(): DefaultExercises {
        return DefaultExercises(appContext.resources, appContext.packageName)
    }

    override suspend fun isExerciseNameInBank(exerciseName: String): Boolean {
        return trackerLocalDataSource.isExerciseNameInBank(exerciseName)
    }

    override suspend fun addExerciseNameToBank(exerciseName: String): Result<String> {
        return try {
            trackerLocalDataSource.addExerciseNameToBank(exerciseName)
            Result.Success(exerciseName)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserExercises(): Result<List<UserExercise>> {
        return try {
            val exercises = trackerLocalDataSource.getAllUserExercises()
            return Result.Success(exercises)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteUserExercise(exerciseName: String): Result<Nothing?> {
        return try {
            trackerLocalDataSource.deleteUserExercise(exerciseName)
            Result.Success(null)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}