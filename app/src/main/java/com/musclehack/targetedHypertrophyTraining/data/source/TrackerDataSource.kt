package com.musclehack.targetedHypertrophyTraining.data.source

import androidx.lifecycle.LiveData
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogCardModel

interface TrackerDataSource {
    suspend fun getCycles(): Result<List<Cycle>>

    fun observeCycles(): LiveData<List<Cycle>>

    suspend fun deleteCycle(cycle: Cycle)

    suspend fun getCycleById(cycleId: Long): Result<Cycle>

    suspend fun changeCycleDuration(cycle: Cycle, newDuration: Int)

    suspend fun changeCycleName(cycle: Cycle, newName: String)

    suspend fun insertSet(exerciseSet: ExerciseSet)

    fun observeWorkouts(cycleId: Long): LiveData<Result<List<Workout>>>

    suspend fun createNestedCycle(nestedCycle: CycleMap)

    suspend fun getTrainingPagerData(cycleId: Long, workoutId: Long): Result<TrainingPagerData>

    fun getExerciseSets(cycleId: Long, workoutId: Long): LiveData<List<ExerciseSet>>

    suspend fun getExerciseSetsSync(cycleId: Long, workoutId: Long): List<ExerciseSet>

    fun getExerciseLogs(exerciseSets: List<ExerciseSet>, day: Int): LiveData<List<ExerciseLog>>

    suspend fun updateExerciseLog(exerciseLog: ExerciseLog)

    suspend fun getNote(setId: Long, day: Int): Result<ExerciseNote>

    suspend fun updateNote(exerciseNote: ExerciseNote)

    suspend fun insertNote(setId: Long, day: Int, newNote: String)

    suspend fun updateWorkout(workout: Workout)

    suspend fun getLogCardModels(exerciseSets: List<ExerciseSet>, day: Int): List<LogCardModel>

    suspend fun saveNewSetPositions(exerciseSets: List<ExerciseSet>)

    suspend fun updateExerciseSet(exerciseSet: ExerciseSet)

    suspend fun deleteExerciseSet(exerciseSet: ExerciseSet)

    suspend fun addExerciseSets(
        position: Int,
        numSets: Int,
        cycleId: Long,
        workoutId: Long,
        exerciseName: String,
        lowerReps: Int,
        higherReps: Int,
        restTime: Int
    )

    suspend fun saveNewWorkoutPositions(workouts: List<Workout>)

    suspend fun insertWorkout(workout: Workout)

    suspend fun deleteWorkout(workout: Workout)

    suspend fun sortCyclesByDateUsed()

    suspend fun sortCyclesByDateCreated()

    suspend fun updateWorkoutName(workout: Workout, newName: String)

    suspend fun updateRepRange(setId: Long, minReps: Int, maxReps: Int)

    suspend fun updateRestTime(setId: Long, restTime: Int)

    suspend fun updateExerciseName(setId: Long, newExerciseName: String)

    suspend fun updateReps(setId: Long, day: Int, reps: Int?)

    suspend fun updateWeight(setId: Long, day: Int, weight: Double?)

    suspend fun createBlankCycle(newCycleName: String, numWeeks: Int)

    @Throws(Exception::class)
    suspend fun changeLogExerciseName(setId: Long, day: Int, newName: String)

    suspend fun skipDay(cycleId: Long, workoutId: Long, day: Int)

    suspend fun saveTrainingDate(cycleId: Long)

    suspend fun skipExercise(setId: Long, day: Int)

    suspend fun undoSkipExercise(setId: Long, day: Int)

    suspend fun getNextExerciseName(setId: Long, currentDay: Int): Result<String>
    suspend fun exportDatabase(): WorkoutDataProto.WorkoutData?
    suspend fun importDatabase(protoBytes: ByteArray)
    suspend fun dropTemporaryTables()
    suspend fun restoreTemporaryTables()
    suspend fun makeNewTables()
    suspend fun makeTemporaryTables()
    suspend fun cloneCycle(cycleId: Long)

    suspend fun getExerciseLog(setId: Long, day: Int): ExerciseLog?

    @Throws(Exception::class)
    suspend fun addExerciseNameToBank(exerciseName: String)

    suspend fun isExerciseNameInBank(exerciseName: String): Boolean

    @Throws(Exception::class)
    suspend fun getAllUserExercises(): List<UserExercise>

    @Throws(Exception::class)
    suspend fun deleteUserExercise(exerciseName: String)
}