package com.musclehack.targetedHypertrophyTraining.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.TrackerLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.TrackerDataSource
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogCardModel
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerChangeEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DefaultTrackerRepository @Inject constructor(
    @TrackerLocalDataSource private val trackerLocalDataSource: TrackerDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val context: Context
) : TrackerRepository {

    private val timerChangeData = MediatorLiveData<Event<TimerChangeEvent>>()

    override suspend fun getCycles(forceUpdate: Boolean): Result<List<Cycle>> {
        return trackerLocalDataSource.getCycles()
    }

    override fun observeCycles(): LiveData<List<Cycle>> {
        return trackerLocalDataSource.observeCycles()
    }

    override suspend fun deleteCycle(cycle: Cycle) {
        trackerLocalDataSource.deleteCycle(cycle)
    }

    override suspend fun changeCycleDuration(cycle: Cycle, newDuration: Int) {
        trackerLocalDataSource.changeCycleDuration(cycle, newDuration)
    }

    override suspend fun changeCycleName(cycle: Cycle, newName: String) {
        trackerLocalDataSource.changeCycleName(cycle, newName)
    }

    override suspend fun insertSet(exerciseSet: ExerciseSet) {
        trackerLocalDataSource.insertSet(exerciseSet)
    }

    override fun observeWorkouts(cycleId: Long): LiveData<Result<List<Workout>>> {
        return trackerLocalDataSource.observeWorkouts(cycleId)
    }

    override suspend fun getCycleById(cycleId: Long): Result<Cycle> {
        return trackerLocalDataSource.getCycleById(cycleId)
    }

    override suspend fun updateWorkout(workout: Workout) {
        trackerLocalDataSource.updateWorkout(workout)
    }

    override suspend fun updateWorkoutName(workout: Workout, newName: String) {
        trackerLocalDataSource.updateWorkoutName(workout, newName)
    }

    private suspend fun getCycleWithId(cycleId: Long): Result<Cycle> {
        return trackerLocalDataSource.getCycleById(cycleId)
    }

    override suspend fun getTrainingPagerData(
        cycleId: Long,
        workoutId: Long
    ): Result<TrainingPagerData> {
        return trackerLocalDataSource.getTrainingPagerData(cycleId, workoutId)
    }

    override fun getExerciseSets(cycleId: Long, workoutId: Long): LiveData<List<ExerciseSet>> {
        return trackerLocalDataSource.getExerciseSets(cycleId, workoutId)
    }

    override fun getExerciseLogs(
        exerciseSets: List<ExerciseSet>,
        day: Int
    ): LiveData<List<ExerciseLog>> {
        return trackerLocalDataSource.getExerciseLogs(exerciseSets, day)
    }

    override suspend fun updateExerciseLog(exerciseLog: ExerciseLog) {
        trackerLocalDataSource.updateExerciseLog(exerciseLog)
    }

    override suspend fun getNote(setId: Long, day: Int): Result<ExerciseNote> {
        return trackerLocalDataSource.getNote(setId, day)
    }

    override suspend fun updateNote(exerciseNote: ExerciseNote) {
        trackerLocalDataSource.updateNote(exerciseNote)
    }

    override suspend fun insertNote(setId: Long, day: Int, newNote: String) {
        trackerLocalDataSource.insertNote(setId, day, newNote)
    }

    override suspend fun getLogCardModels(
        exerciseSets: List<ExerciseSet>,
        day: Int
    ): List<LogCardModel> {
        return trackerLocalDataSource.getLogCardModels(exerciseSets, day)
    }

    override suspend fun saveNewSetPositions(exerciseSets: List<ExerciseSet>) {
        trackerLocalDataSource.saveNewSetPositions(exerciseSets)
    }

    override suspend fun updateExerciseSet(exerciseSet: ExerciseSet) {
        trackerLocalDataSource.updateExerciseSet(exerciseSet)
    }

    override suspend fun deleteExerciseSet(exerciseSet: ExerciseSet) {
        trackerLocalDataSource.deleteExerciseSet(exerciseSet)
    }

    override suspend fun addExerciseSets(
        position: Int,
        numSets: Int,
        cycleId: Long,
        workoutId: Long,
        exerciseName: String,
        lowerReps: Int,
        higherReps: Int,
        restTime: Int
    ) {
        trackerLocalDataSource.addExerciseSets(
            position,
            numSets,
            cycleId,
            workoutId,
            exerciseName,
            lowerReps,
            higherReps,
            restTime
        )
    }

    override suspend fun saveNewWorkoutPositions(workouts: List<Workout>) {
        trackerLocalDataSource.saveNewWorkoutPositions(workouts)
    }

    override suspend fun insertWorkout(workout: Workout) {
        trackerLocalDataSource.insertWorkout(workout)
    }

    override suspend fun deleteWorkout(workout: Workout) {
        trackerLocalDataSource.deleteWorkout(workout)
    }

    override suspend fun sortCyclesByDateUsed() {
        trackerLocalDataSource.sortCyclesByDateUsed()
    }

    override suspend fun sortCyclesByDateCreated() {
        trackerLocalDataSource.sortCyclesByDateCreated()
    }

    override suspend fun updateRepRange(setId: Long, minReps: Int, maxReps: Int) {
        trackerLocalDataSource.updateRepRange(setId, minReps, maxReps)
    }

    override suspend fun updateRestTime(setId: Long, restTimeSeconds: Int) {
        trackerLocalDataSource.updateRestTime(setId, restTimeSeconds)
    }

    override suspend fun updateExerciseName(setId: Long, newExerciseName: String) {
        trackerLocalDataSource.updateExerciseName(setId, newExerciseName)
    }

    override suspend fun updateReps(setId: Long, day: Int, reps: Int?) {
        trackerLocalDataSource.updateReps(setId, day, reps)
    }

    override suspend fun updateWeight(setId: Long, day: Int, weight: Double?) {
        trackerLocalDataSource.updateWeight(setId, day, weight)
    }

    override suspend fun changeLogExerciseName(
        setId: Long,
        day: Int,
        newName: String
    ): Result<ExerciseLog?> {
        return try {
            trackerLocalDataSource.changeLogExerciseName(setId, day, newName)
            val newLog = trackerLocalDataSource.getExerciseLog(setId, day)
            Result.Success(newLog)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun skipDay(cycleId: Long, workoutId: Long, day: Int) {
        trackerLocalDataSource.skipDay(cycleId, workoutId, day)
    }

    override suspend fun saveTrainingDate(cycleId: Long) {
        trackerLocalDataSource.saveTrainingDate(cycleId)
    }

    override suspend fun skipExercise(setId: Long, day: Int) {
        trackerLocalDataSource.skipExercise(setId, day)
    }

    override suspend fun undoSkipExercise(setId: Long, day: Int) {
        trackerLocalDataSource.undoSkipExercise(setId, day)
    }

    override fun observeTimerChangeEvent(): LiveData<Event<TimerChangeEvent>> {
        return timerChangeData
    }

    override fun addTimerChangeSource(data: LiveData<Event<TimerChangeEvent>>) {
        timerChangeData.addSource(data) { timerChangeData.value = data.value }
    }

    override fun removeTimerChangeSource(data: LiveData<Event<TimerChangeEvent>>) {
        timerChangeData.removeSource(data)
    }

    override suspend fun getNextExerciseName(setId: Long, currentDay: Int): Result<String> {
        return trackerLocalDataSource.getNextExerciseName(setId, currentDay)
    }

    override suspend fun exportDatabase(): Result<WorkoutDataProto.WorkoutData> {
        try {
            val data = trackerLocalDataSource.exportDatabase()
                ?: return Result.Error(Exception("Error exporting database."))
            return Result.Success(data)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun importDatabase(bytes: ByteArray): Result<Nothing?> {
        try {
            trackerLocalDataSource.makeTemporaryTables()
            trackerLocalDataSource.makeNewTables()
            trackerLocalDataSource.importDatabase(bytes)
            trackerLocalDataSource.dropTemporaryTables()
            return Result.Success(null)
        } catch (e: Exception) {
            trackerLocalDataSource.restoreTemporaryTables()
            return Result.Error(e)
        }
    }

    override suspend fun cloneCycle(cycleId: Long) {
        try {
            trackerLocalDataSource.cloneCycle(cycleId)
        } catch (e: Exception) {
            // do something
        }
    }

}