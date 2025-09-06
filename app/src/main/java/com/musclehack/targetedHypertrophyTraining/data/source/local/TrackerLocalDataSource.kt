package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.TrackerDataSource
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogCardModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Concrete implementation of a data source as a db.
 *
 */

private const val cyclesTemp = "CYCLES_TEMP"
private const val workoutsTemp = "WORKOUTS_TEMP"
private const val setsTemp = "SETS_TEMP"
private const val logsTemp = "LOGS_TEMP"
private const val notesTemp = "NOTES_TEMP"
private const val userExercisesTemp = "USER_EXERCISES_TEMP"

class TrackerLocalDataSource internal constructor(
    private val cycleDao: CycleDao,
    private val workoutDao: WorkoutDao,
    private val exerciseSetDao: ExerciseSetDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val exerciseNoteDao: ExerciseNoteDao,
    private val userExerciseDao: UserExerciseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val appDatabase: AppDatabase
) : TrackerDataSource {
    override suspend fun getCycles(): Result<List<Cycle>> = withContext(ioDispatcher) {
        try {
            Result.Success(cycleDao.getCycles())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeCycles(): LiveData<List<Cycle>> {
        return cycleDao.observeCycles()
    }

    override suspend fun deleteCycle(cycle: Cycle): Unit = withContext(ioDispatcher) {
        cycleDao.deleteCycle(cycle)
    }

    override suspend fun getCycleById(cycleId: Long): Result<Cycle> = withContext(ioDispatcher) {
        try {
            val cycle = cycleDao.getCycleById(cycleId)
            if (cycle != null) {
                return@withContext Result.Success(cycle)
            } else {
                return@withContext Result.Error(Exception("Cycle not found"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    override suspend fun changeCycleDuration(cycle: Cycle, newDuration: Int) {
        withContext(ioDispatcher) {
            cycle.numWeeks = newDuration
            cycleDao.updateCycle(cycle)
        }
    }

    override suspend fun changeCycleName(cycle: Cycle, newName: String) =
        withContext(ioDispatcher) {
            cycleDao.updateCycleName(cycle.id, newName)
        }

    override suspend fun insertSet(exerciseSet: ExerciseSet) = withContext(ioDispatcher) {
        exerciseSetDao.insertSet(exerciseSet)
    }

    override fun observeWorkouts(cycleId: Long): LiveData<Result<List<Workout>>> {
        return workoutDao.getWorkouts(cycleId).map {
            Result.Success(it)
        }
    }

    /** cycle: Cycle, workouts: List<WorkoutWithSets>,  */
    /**
     * map: HashMap<Cycle,
     *
     */
    override suspend fun createNestedCycle(nestedCycle: CycleMap) {
        withContext(ioDispatcher) {
            appDatabase.runInTransaction {
                for ((cycle, workoutsMapsList) in nestedCycle) {
                    // get all cycles and shift them down
                    val cycles = cycleDao.getCycles()
                    cycles.forEachIndexed { index, c ->
                        cycleDao.updatePositionSync(c.id, index + 1)
                    }
                    val cycleId = cycleDao.insertCycleSync(cycle)
                    for (workoutsMap in workoutsMapsList) {
                        for ((workout, exerciseSetList) in workoutsMap) {
                            workout.cycleId = cycleId
                            val workoutId = workoutDao.insertWorkoutSync(workout)
                            exerciseSetList.forEachIndexed { idx, exerciseSet ->
                                exerciseSet.cycleId = cycleId
                                exerciseSet.workoutId = workoutId
                                exerciseSet.position = idx
                                val setId = exerciseSetDao.insertSetSync(exerciseSet)
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun getTrainingPagerData(
        cycleId: Long,
        workoutId: Long
    ): Result<TrainingPagerData> =
        withContext(ioDispatcher) {
            try {
                val cycle = cycleDao.getCycleById(cycleId)
                val workout = workoutDao.getWorkoutById(workoutId)
                if (cycle == null || workout == null)
                    return@withContext Result.Error(Exception("Failed to create training pager data object"))
                return@withContext Result.Success(Pair(cycle, workout))
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun saveNewSetPositions(exerciseSets: List<ExerciseSet>) {
        withContext(ioDispatcher) {
            appDatabase.runInTransaction {
                exerciseSets.forEachIndexed { index, exerciseSet ->
                    exerciseSet.position = index
                    exerciseSetDao.updateSetSync(exerciseSet)
                }
            }
        }
    }

    override suspend fun saveNewWorkoutPositions(workouts: List<Workout>) {
        withContext(ioDispatcher) {
            appDatabase.runInTransaction {
                workouts.forEachIndexed { index, workout ->
                    workout.position = index
                    workoutDao.updateWorkoutSync(workout)
                }
            }
        }
    }

    override suspend fun insertWorkout(workout: Workout) {
        withContext(ioDispatcher) {
            workoutDao.insertWorkout(workout)
        }
    }

    override suspend fun deleteWorkout(workout: Workout) {
        withContext(ioDispatcher) {
            workoutDao.deleteWorkout(workout)
            val workouts = workoutDao.getWorkoutsSync(workout.cycleId)
            // change positions as well
            workouts.forEachIndexed { index, workout ->
                workout.position = index
                workoutDao.updateWorkout(workout)
            }
        }
    }

    override suspend fun sortCyclesByDateUsed() {
        withContext(ioDispatcher) {
            val cyclesResult = getCycles()
            if (cyclesResult is Result.Success) {
                val cycles: List<Cycle> = cyclesResult.data
                val sortedCycles: List<Cycle> =
                    cycles.sortedByDescending { cycle -> cycle.dateLastLogged }
                sortedCycles.forEachIndexed { index, cycle ->
                    cycle.position = index
                    cycleDao.updateCycle(cycle)
                }
            }
        }
    }

    override suspend fun sortCyclesByDateCreated() {
        withContext(ioDispatcher) {
            val cyclesResult = getCycles()
            if (cyclesResult is Result.Success) {
                val cycles: List<Cycle> = cyclesResult.data
                val sortedCycles: List<Cycle> =
                    cycles.sortedByDescending { cycle -> cycle.dateCreated }
                sortedCycles.forEachIndexed { index, cycle ->
                    cycle.position = index
                    cycleDao.updateCycle(cycle)
                }
            }
        }
    }

    override suspend fun updateWorkoutName(workout: Workout, newName: String) {
        withContext(ioDispatcher) {
            workoutDao.updateWorkoutName(workout.id, newName)
        }
    }

    override suspend fun updateRepRange(setId: Long, minReps: Int, maxReps: Int) {
        withContext(ioDispatcher) {
            exerciseSetDao.updateMinReps(setId, minReps)
            exerciseSetDao.updateMaxReps(setId, maxReps)
        }
    }

    override suspend fun updateRestTime(setId: Long, restTime: Int) {
        withContext(ioDispatcher) { exerciseSetDao.updateRestTime(setId, restTime) }
    }

    override suspend fun updateExerciseName(setId: Long, newExerciseName: String) {
        withContext(ioDispatcher) { exerciseSetDao.updateExerciseName(setId, newExerciseName) }
    }

    override suspend fun updateReps(setId: Long, day: Int, reps: Int?) {
        withContext(ioDispatcher) {
            var exLog = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                // TODO: Get any notes and add them as well
                exLog = ExerciseLog.createDefault(setId, day).copy(reps = reps)
                exerciseLogDao.insertLog(exLog)
            } else {
                val updateCount = exerciseLogDao.updateReps(setId, day, reps)
                if (updateCount == 0) {
                    // error
                }
            }
        }
    }

    override suspend fun updateWeight(setId: Long, day: Int, weight: Double?) {
        withContext(ioDispatcher) {
            // check if exists first
            var exLog = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                exLog = ExerciseLog.createDefault(setId, day).copy(weight = weight)
                exerciseLogDao.insertLog(exLog)
            } else {
                val updateCount = exerciseLogDao.updateWeight(setId, day, weight)
                if (updateCount == 0) {
                    // error
                }
            }
        }
    }

    override suspend fun createBlankCycle(newCycleName: String, numWeeks: Int) {
        withContext(ioDispatcher) {
            val cycles = cycleDao.getCycles()
            cycles.forEachIndexed { index, c ->
                cycleDao.updatePositionSync(c.id, index + 1)
            }
            val cycle = Cycle.createDefault(newCycleName, numWeeks)
            cycleDao.insertCycleSync(cycle)
        }
    }

    /** This substitutes an exercise for one day. */
    @Throws(Exception::class)
    override suspend fun changeLogExerciseName(setId: Long, day: Int, newName: String) {
        withContext(ioDispatcher) {
            var exLog: ExerciseLog? = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                exLog = ExerciseLog.createDefault(setId, day).copy(subName = newName)
                exerciseLogDao.insertLogSync(exLog)
            } else {
                val exSet = exerciseSetDao.getExerciseSet(setId)
                    ?: // error
                    throw Exception("Could not retrieve set for ID $setId")
                val isSubstitute: Boolean = exSet.exerciseName != newName
                if (isSubstitute) {
                    exerciseLogDao.updateSubstitutionSingleDay(newName, setId, day)
                } else {
                    exerciseLogDao.updateSubstitutionSingleDay(null, setId, day)
                }
            }
        }
    }

    private fun createExLogIfNotExists(setId: Long, day: Int): ExerciseLog {
        var exLog: ExerciseLog? = exerciseLogDao.getExerciseLog(setId, day)
        if (exLog == null) {
            exLog = ExerciseLog.createDefault(setId, day)
        }
        return exLog
    }

    override suspend fun skipDay(cycleId: Long, workoutId: Long, day: Int) {
        withContext(ioDispatcher) {
            val exSets = getExerciseSetsSync(cycleId, workoutId)
            for (set in exSets) {
                val exLog = createExLogIfNotExists(set.id, day)
                exLog.skip = true
                exerciseLogDao.insertLogSync(exLog)
            }
        }
    }

    override suspend fun saveTrainingDate(cycleId: Long) {
        withContext(ioDispatcher) {
            cycleDao.updateTrainingDate(cycleId, Calendar.getInstance().timeInMillis)
        }
    }

    override suspend fun skipExercise(setId: Long, day: Int) {
        withContext(ioDispatcher) {
            var exLog: ExerciseLog? = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                exLog = ExerciseLog.createDefault(setId, day)
                exLog.skip = true
                exerciseLogDao.insertLogSync(exLog)
            } else {
                exerciseLogDao.updateSkip(setId, day, true)
            }
        }
    }

    override suspend fun undoSkipExercise(setId: Long, day: Int) {
        withContext(ioDispatcher) {
            var exLog: ExerciseLog? = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                exLog = ExerciseLog.createDefault(setId, day)
                exLog.skip = false
                exerciseLogDao.update(exLog)
            } else {
                exerciseLogDao.updateSkip(setId, day, false)
            }
        }
    }

    override suspend fun getNextExerciseName(setId: Long, currentDay: Int): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val exSet = exerciseSetDao.getExerciseSet(setId)
                    ?: return@withContext Result.Error(Exception("could not find set"))
                val nextSet = exerciseSetDao.getExerciseSet(
                    exSet.cycleId,
                    exSet.workoutId, exSet.position + 1
                )
                    ?: return@withContext Result.Error(Exception("could not find next set"))
                val nextLog = exerciseLogDao.getExerciseLog(nextSet.id, currentDay)
                if (nextLog?.subName != null) {
                    return@withContext Result.Success(nextLog.subName!!)
                } else {
                    return@withContext Result.Success(nextSet.exerciseName)
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }

        }
    }

    override suspend fun updateExerciseSet(exerciseSet: ExerciseSet) = withContext(ioDispatcher) {
        exerciseSetDao.updateExerciseSet(exerciseSet)
    }

    override suspend fun deleteExerciseSet(exerciseSet: ExerciseSet) {
        withContext(ioDispatcher) {
            // TODO: Consider runInTransaction
            exerciseSetDao.deleteExerciseSet(exerciseSet)
            // recompute positions
            val exerciseSets = getExerciseSetsSync(
                exerciseSet.cycleId,
                exerciseSet.workoutId
            )
            exerciseSets.forEachIndexed { index, exerciseSet ->
                exerciseSet.position = index
                exerciseSetDao.updateExerciseSet(exerciseSet)
            }
        }
    }

    override suspend fun addExerciseSets(
        position: Int, numSets: Int, cycleId: Long,
        workoutId: Long, exerciseName: String, lowerReps: Int, higherReps: Int, restTime: Int
    ) {
        // we will need to get all sets, re-order them all, then insert again.
        withContext(ioDispatcher) {
            val exerciseSets = getExerciseSetsSync(cycleId, workoutId).toList()
            var newSetIdx = position
            for (i in 0 until numSets) {
                val newSet = ExerciseSet(
                    cycleId = cycleId,
                    workoutId = workoutId,
                    position = newSetIdx++,
                    higherReps = higherReps,
                    lowerReps = lowerReps,
                    exerciseName = exerciseName,
                    restTime = restTime,
                    id = 0
                )
                exerciseSetDao.insertSetSync(newSet)
            }
            // recompute positions
            exerciseSets.forEachIndexed { index, exerciseSet ->
                if (index >= position) {
                    exerciseSetDao.updatePosition(exerciseSet.id, index + numSets)
                }
            }
        }
    }

    override suspend fun getLogCardModels(
        exerciseSets: List<ExerciseSet>,
        day: Int
    ): List<LogCardModel> {
        return withContext(ioDispatcher) {
            val logCardModels = mutableListOf<LogCardModel>()
            appDatabase.runInTransaction {
                val setIds = exerciseSets.map { it.id }
                for ((position, setId) in setIds.withIndex()) {
                    // Note: list is returned in descending order
                    var log: ExerciseLog? = null
                    val logs = exerciseLogDao.getExerciseLogsBeforeAndIncludingDay(day, setId)
                    //
                    // find the most recent log that has a note
                    var mostRecentNoteDay = -1
                    var mostRecentRecordedLog: ExerciseLog? = null
                    for (item in logs) {
                        if (item.day == day) {
                            log = item
                        } else {
                            if (mostRecentNoteDay == -1 && item.hasNote) {
                                mostRecentNoteDay = item.day
                            }
                            if (mostRecentRecordedLog == null &&
                                (item.weight != null || item.reps != null) &&
                                item.subName == null && !item.skip
                            ) {
                                /** Only use this item if it is NOT a substitute exercise, and
                                 * it is NOT a skipped item. */
                                mostRecentRecordedLog = item
                            }
                        }

                        // This will stop our loop when we have the data we need
                        if (mostRecentRecordedLog != null && mostRecentNoteDay >= 0) {
                            break
                        }
                    }
                    if (log == null) {
                        // create a new log here
                        log = ExerciseLog.createDefault(setId, day)
                        // it should have null weight, null reps, hasNote = false
                    }
                    val prevSetData = PrevSetData(
                        prevWeight = mostRecentRecordedLog?.weight,
                        prevReps = mostRecentRecordedLog?.reps, prevNoteDay = mostRecentNoteDay
                    )
                    logCardModels.add(
                        LogCardModel(
                            exerciseLog = log, exerciseSet = exerciseSets[position],
                            prevSetData = prevSetData
                        )
                    )
                }
            }
            return@withContext logCardModels
        }
    }

    override fun getExerciseSets(cycleId: Long, workoutId: Long): LiveData<List<ExerciseSet>> {
        return exerciseSetDao.getExerciseSets(cycleId, workoutId)
    }

    override suspend fun getExerciseSetsSync(cycleId: Long, workoutId: Long): List<ExerciseSet> {
        return exerciseSetDao.getExerciseSetsSync(cycleId, workoutId)
    }

    override fun getExerciseLogs(
        exerciseSets: List<ExerciseSet>,
        day: Int
    ): LiveData<List<ExerciseLog>> {
        // we need a list of setIds, and the day
        val ids: List<Long> = exerciseSets.map { s -> s.id }
        return exerciseLogDao.getExerciseLogs(ids, day)
    }

    override suspend fun updateExerciseLog(exerciseLog: ExerciseLog) =
        withContext(ioDispatcher) {
            exerciseLogDao.insertLog(exerciseLog)
        }

    override suspend fun getNote(setId: Long, day: Int): Result<ExerciseNote> =
        withContext(ioDispatcher) {
            try {
                val note = exerciseNoteDao.getExerciseNote(setId, day)
                    ?: return@withContext Result.Error(Exception("no note found"))
                return@withContext Result.Success(note)
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun updateNote(exerciseNote: ExerciseNote) = withContext(ioDispatcher) {
        exerciseNoteDao.updateExerciseNote(exerciseNote)
    }

    // Note: This is technically also updating exerciseNotes as well!
    override suspend fun insertNote(setId: Long, day: Int, newNote: String) =
        withContext(ioDispatcher) {
            // First check if our exerciseLog exists. If not, create it.
            var exLog = exerciseLogDao.getExerciseLog(setId, day)
            if (exLog == null) {
                exLog = ExerciseLog.createDefault(setId, day).copy(hasNote = newNote.isNotEmpty())
                exerciseLogDao.insertLog(exLog)
            } else {
                exerciseLogDao.updateNoteStatus(setId, day, newNote.isNotEmpty())
            }
            exerciseNoteDao.insertExerciseNote(
                ExerciseNote(
                    setId, day, newNote,
                    Calendar.getInstance().timeInMillis
                )
            )
        }

    override suspend fun updateWorkout(workout: Workout) {
        withContext(ioDispatcher) {
            workoutDao.updateWorkout(workout)
        }
    }

    override suspend fun makeTemporaryTables() = withContext(ioDispatcher) {
        val db = appDatabase.openHelper.writableDatabase
        db.execSQL("ALTER TABLE cycles RENAME TO $cyclesTemp")
        db.execSQL("ALTER TABLE workouts RENAME TO $workoutsTemp")
        db.execSQL("ALTER TABLE sets RENAME TO $setsTemp")
        db.execSQL("ALTER TABLE exerciseLogs RENAME TO $logsTemp")
        db.execSQL("ALTER TABLE exerciseNotes RENAME TO $notesTemp")
        db.execSQL("ALTER TABLE userExercises RENAME TO $userExercisesTemp")
    }

    @Throws(Exception::class)
    override suspend fun cloneCycle(cycleId: Long) = withContext(ioDispatcher) {
        val cycle = cycleDao.getCycleById(cycleId) ?: throw Exception("Cycle not found")
        val newCycleId = cycleDao.insertCycle(cycle.copy(id = 0, dateLastLogged = 0))
        val workouts: List<Workout> = workoutDao.getWorkoutsSync(cycleId)
        for (w in workouts) {
            val newWorkoutId = workoutDao.insertWorkoutSync(w.copy(id = 0, cycleId = newCycleId))
            for (s in exerciseSetDao.getExerciseSetsSync(cycleId, w.id)) {
                val newSetId = exerciseSetDao.insertSetSync(
                    s.copy(id = 0, workoutId = newWorkoutId, cycleId = newCycleId)
                )
                exerciseLogDao.getExerciseLogs(s.id).forEach { l ->
                    exerciseLogDao.insertLogSync(l.copy(setId = newSetId))
                }
                exerciseNoteDao.getExerciseNotes(s.id).forEach { note ->
                    exerciseNoteDao.insertExerciseNote(note.copy(setId = newSetId))
                }
            }
        }
    }

    override suspend fun getExerciseLog(setId: Long, day: Int): ExerciseLog? {
        return withContext(ioDispatcher) {
            return@withContext exerciseLogDao.getExerciseLog(setId, day)
        }
    }

    override suspend fun addExerciseNameToBank(exerciseName: String) {
        withContext(ioDispatcher) {
            userExerciseDao.insertExercise(UserExercise(id = 0, name = exerciseName))
        }
    }

    override suspend fun isExerciseNameInBank(exerciseName: String): Boolean {
        return withContext(ioDispatcher) {
            val userExercise = userExerciseDao.getUserExercise(exerciseName)
            return@withContext userExercise != null
        }
    }

    override suspend fun getAllUserExercises(): List<UserExercise> = withContext(ioDispatcher) {
        return@withContext userExerciseDao.getAllUserExercises()
    }

    override suspend fun deleteUserExercise(exerciseName: String) =
        withContext(ioDispatcher) {
            userExerciseDao.deleteUserExercise(exerciseName)
        }

    /**
     * Please note: If the schema is upgraded, we
     * need to change these statements!!!
     */
    override suspend fun makeNewTables() = withContext(ioDispatcher) {
        val db = appDatabase.openHelper.writableDatabase
        db.execSQL("CREATE TABLE IF NOT EXISTS cycles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, position INTEGER NOT NULL, dateCreated INTEGER NOT NULL, dateLastLogged INTEGER NOT NULL, numWeeks INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS workouts (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, position INTEGER NOT NULL, repeats INTEGER NOT NULL, lastDayViewed INTEGER NOT NULL, lastSetViewed INTEGER NOT NULL, cycleId INTEGER NOT NULL, FOREIGN KEY(cycleId) REFERENCES cycles(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, position INTEGER NOT NULL, cycleId INTEGER NOT NULL, workoutId INTEGER NOT NULL, exerciseName TEXT NOT NULL, lowerReps INTEGER NOT NULL, higherReps INTEGER NOT NULL, restTime INTEGER NOT NULL, FOREIGN KEY(workoutId) REFERENCES workouts(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS exerciseLogs (setId INTEGER NOT NULL, day INTEGER NOT NULL, logDate INTEGER, weight REAL, reps INTEGER, hasNote INTEGER NOT NULL, subName TEXT, skip INTEGER NOT NULL, PRIMARY KEY(setId, day), FOREIGN KEY(setId) REFERENCES sets(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS exerciseNotes (setId INTEGER NOT NULL, day INTEGER NOT NULL, note TEXT NOT NULL, date INTEGER NOT NULL, PRIMARY KEY(setId, day), FOREIGN KEY(setId, day) REFERENCES exerciseLogs(setId, day) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS userExercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
    }

    override suspend fun dropTemporaryTables() = withContext(ioDispatcher) {
        val db = appDatabase.openHelper.writableDatabase
        db.execSQL("DROP TABLE $cyclesTemp")
        db.execSQL("DROP TABLE $workoutsTemp")
        db.execSQL("DROP TABLE $setsTemp")
        db.execSQL("DROP TABLE $logsTemp")
        db.execSQL("DROP TABLE $notesTemp")
        db.execSQL("DROP TABLE $userExercisesTemp")
    }

    /**
     * Restores tables if our backup fails.
     */
    @Throws(Exception::class)
    override suspend fun restoreTemporaryTables() = withContext(ioDispatcher) {
        val db = appDatabase.openHelper.writableDatabase
        // drop our new tables
        db.execSQL("DROP TABLE cycles")
        db.execSQL("DROP TABLE workouts")
        db.execSQL("DROP TABLE sets")
        db.execSQL("DROP TABLE exerciseLogs")
        db.execSQL("DROP TABLE exerciseNotes")
        db.execSQL("ALTER TABLE $cyclesTemp RENAME TO cycles")
        db.execSQL("ALTER TABLE $workoutsTemp RENAME TO workouts")
        db.execSQL("ALTER TABLE $setsTemp RENAME TO sets")
        db.execSQL("ALTER TABLE $logsTemp RENAME TO exerciseLogs")
        db.execSQL("ALTER TABLE $notesTemp RENAME TO exerciseNotes")
    }

    // import and export database methods
    override suspend fun exportDatabase(): WorkoutDataProto.WorkoutData? =
        withContext(ioDispatcher) {
            try {
                val workoutDataProto = WorkoutDataProto.WorkoutData.newBuilder()
                    .addAllCycles(cycleDao.getCycles().map {
                        it.toProto()
                    })
                    .addAllWorkouts(workoutDao.getAllWorkouts().map {
                        it.toProto()
                    })
                    .addAllExerciseSets(exerciseSetDao.getAllExerciseSets().map {
                        it.toProto()
                    })
                    .addAllExerciseLogs(exerciseLogDao.getAllExerciseLogs().map {
                        it.toProto()
                    })
                    .addAllExerciseNotes(exerciseNoteDao.getAllExerciseNotes().map {
                        it.toProto()
                    })
                    .addAllUserExercises(userExerciseDao.getAllUserExercises().map {
                        it.toProto()
                    })
                    .build()
                return@withContext workoutDataProto
            } catch (e: Exception) {
                throw (e)
            }
        }

    @Throws(Exception::class)
    override suspend fun importDatabase(protoBytes: ByteArray) = withContext(ioDispatcher) {
        val workoutData = WorkoutDataProto.WorkoutData.parseFrom(protoBytes)
        workoutData.cyclesList.forEach {
            cycleDao.insertCycle(Cycle.fromProto(it))
        }
        workoutData.workoutsList.forEach {
            workoutDao.insertWorkout(Workout.fromProto(it))
        }
        workoutData.exerciseSetsList.forEach {
            exerciseSetDao.insertSet(ExerciseSet.fromProto(it))
        }
        workoutData.exerciseLogsList.forEach {
            exerciseLogDao.insertLog(ExerciseLog.fromProto(it))
        }
        workoutData.exerciseNotesList.forEach {
            exerciseNoteDao.insertExerciseNote(ExerciseNote.fromProto(it))
        }
        workoutData.userExercisesList.forEach {
            userExerciseDao.insertExercise(UserExercise.fromProto(it))
        }
    }
}
