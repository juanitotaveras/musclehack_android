package com.musclehack.targetedHypertrophyTraining.data.repository

import androidx.lifecycle.LiveData
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogCardModel
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerChangeEvent

/**
 * Interface to data layer.
 */
interface TrackerRepository {
    suspend fun getCycles(forceUpdate: Boolean = false): Result<List<Cycle>>
    fun observeCycles(): LiveData<List<Cycle>>
    suspend fun deleteCycle(cycle: Cycle)
    suspend fun changeCycleDuration(cycle: Cycle, newDuration: Int)
    suspend fun changeCycleName(cycle: Cycle, newName: String)

    suspend fun insertSet(exerciseSet: ExerciseSet)

    fun observeWorkouts(cycleId: Long): LiveData<Result<List<Workout>>>

    suspend fun getCycleById(cycleId: Long): Result<Cycle>

    suspend fun updateWorkout(workout: Workout)

    suspend fun updateWorkoutName(workout: Workout, newName: String)

    suspend fun getTrainingPagerData(cycleId: Long, workoutId: Long): Result<TrainingPagerData>

    fun getExerciseSets(cycleId: Long, workoutId: Long): LiveData<List<ExerciseSet>>

    fun getExerciseLogs(exerciseSets: List<ExerciseSet>, day: Int): LiveData<List<ExerciseLog>>

    suspend fun updateExerciseLog(exerciseLog: ExerciseLog)

    suspend fun getNote(setId: Long, day: Int): Result<ExerciseNote>

    suspend fun updateNote(exerciseNote: ExerciseNote)

    suspend fun insertNote(setId: Long, day: Int, newNote: String)

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

    suspend fun updateRepRange(setId: Long, minReps: Int, maxReps: Int)

    suspend fun updateRestTime(setId: Long, restTimeSeconds: Int)

    suspend fun updateExerciseName(setId: Long, newExerciseName: String)

    suspend fun updateReps(setId: Long, day: Int, reps: Int?)

    suspend fun updateWeight(setId: Long, day: Int, weight: Double?)
    suspend fun changeLogExerciseName(setId: Long, day: Int, newName: String): Result<ExerciseLog?>

    suspend fun skipDay(cycleId: Long, workoutId: Long, day: Int)

    suspend fun saveTrainingDate(cycleId: Long)

    suspend fun skipExercise(setId: Long, day: Int)

    suspend fun undoSkipExercise(setId: Long, day: Int)

    fun observeTimerChangeEvent(): LiveData<Event<TimerChangeEvent>>

    fun addTimerChangeSource(data: LiveData<Event<TimerChangeEvent>>)

    fun removeTimerChangeSource(data: LiveData<Event<TimerChangeEvent>>)

    suspend fun getNextExerciseName(setId: Long, currentDay: Int): Result<String>
    suspend fun exportDatabase(): Result<WorkoutDataProto.WorkoutData>

    suspend fun importDatabase(bytes: ByteArray): Result<Nothing?>

    suspend fun cloneCycle(cycleId: Long)

}