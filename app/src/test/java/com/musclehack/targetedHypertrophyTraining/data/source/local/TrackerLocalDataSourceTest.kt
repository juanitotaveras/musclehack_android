package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.musclehack.targetedHypertrophyTraining.MainCoroutineRule
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import com.musclehack.targetedHypertrophyTraining.getOrAwaitValue
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TrackerLocalDataSourceTest {

    private lateinit var db: AppDatabase
    private lateinit var trackerLocalDataSource: TrackerLocalDataSource
    private val testDispatcher = UnconfinedTestDispatcher()

    // Set main coroutines dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        trackerLocalDataSource = TrackerLocalDataSource(
            cycleDao = db.cycleDao(),
            workoutDao = db.workoutDao(),
            exerciseSetDao = db.setDao(),
            exerciseLogDao = db.exerciseLogDao(),
            exerciseNoteDao = db.exerciseNoteDao(),
            userExerciseDao = db.userExerciseDao(),
            ioDispatcher = testDispatcher,
            appDatabase = db
        )
    }

    @After
    fun closeDb() = db.close()

    private suspend fun createTestCycle(name: String = "Test Cycle", numWeeks: Int = 8): Long {
        val cycle = Cycle.createDefault(name, numWeeks)
        return db.cycleDao().insertCycle(cycle)
    }

    private suspend fun createTestWorkout(
        cycleId: Long,
        name: String = "Test Workout",
        position: Int = 0
    ): Long {
        val workout = Workout.createDefault(name, 3, position).apply { this.cycleId = cycleId }
        return db.workoutDao().insertWorkout(workout)
    }

    private suspend fun createTestExerciseSet(
        cycleId: Long,
        workoutId: Long,
        name: String = "Test Exercise",
        position: Int = 0
    ): Long {
        val exerciseSet = ExerciseSet.createDefault(name, 8, 12, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = position
        }
        return db.setDao().insertSetSync(exerciseSet)
    }

    // CYCLE OPERATIONS TESTS
    @Test
    fun `getCycles returns success with list of cycles`() = runTest {
        val cycleId1 = createTestCycle("Cycle 1")
        val cycleId2 = createTestCycle("Cycle 2")

        val result = trackerLocalDataSource.getCycles()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val cycles = (result as Result.Success).data
        assertThat(cycles).hasSize(2)
        assertThat(cycles.map { it.name }).containsExactly("Cycle 1", "Cycle 2")
    }

    @Test
    fun `observeCycles returns LiveData with cycles`() = runTest {
        val cycleId = createTestCycle("Observable Cycle")

        val cyclesLiveData = trackerLocalDataSource.observeCycles()
        cyclesLiveData.getOrAwaitValue()

        val cycles = cyclesLiveData.value
        assertThat(cycles).isNotNull()
        assertThat(cycles).hasSize(1)
        assertThat(cycles!![0].name).isEqualTo("Observable Cycle")
    }

    @Test
    fun `getCycleById returns success when cycle exists`() = runTest {
        val cycleId = createTestCycle("Find Me")

        val result = trackerLocalDataSource.getCycleById(cycleId)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val cycle = (result as Result.Success).data
        assertThat(cycle.name).isEqualTo("Find Me")
        assertThat(cycle.id).isEqualTo(cycleId)
    }

    @Test
    fun `getCycleById returns error when cycle does not exist`() = runTest {
        val result = trackerLocalDataSource.getCycleById(999L)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = (result as Result.Error).exception
        assertThat(error.message).isEqualTo("Cycle not found")
    }

    @Test
    fun `deleteCycle removes cycle from database`() = runTest {
        val cycleId = createTestCycle("To Delete")
        val cycle = db.cycleDao().getCycleById(cycleId)!!

        trackerLocalDataSource.deleteCycle(cycle)

        val retrievedCycle = db.cycleDao().getCycleById(cycleId)
        assertThat(retrievedCycle).isNull()
    }

    @Test
    fun `changeCycleDuration updates cycle duration`() = runTest {
        val cycleId = createTestCycle("Duration Test", 8)
        val cycle = db.cycleDao().getCycleById(cycleId)!!

        trackerLocalDataSource.changeCycleDuration(cycle, 12)

        val updatedCycle = db.cycleDao().getCycleById(cycleId)
        assertThat(updatedCycle!!.numWeeks).isEqualTo(12)
    }

    @Test
    fun `changeCycleName updates cycle name`() = runTest {
        val cycleId = createTestCycle("Original Name")

        trackerLocalDataSource.changeCycleName(db.cycleDao().getCycleById(cycleId)!!, "New Name")

        val updatedCycle = db.cycleDao().getCycleById(cycleId)
        assertThat(updatedCycle!!.name).isEqualTo("New Name")
    }

    @Test
    fun `createBlankCycle creates cycle and shifts existing positions`() = runTest {
        // Create existing cycles
        createTestCycle("Existing 1").also { db.cycleDao().updatePositionSync(it, 0) }
        createTestCycle("Existing 2").also { db.cycleDao().updatePositionSync(it, 1) }

        trackerLocalDataSource.createBlankCycle("New Cycle", 10)

        val allCycles = db.cycleDao().getCycles()
        assertThat(allCycles).hasSize(3)

        // New cycle should be at position 0
        val newCycle = allCycles.find { it.name == "New Cycle" }
        assertThat(newCycle).isNotNull()
        assertThat(newCycle!!.position).isEqualTo(0)

        // Existing cycles should be shifted down
        val existing1 = allCycles.find { it.name == "Existing 1" }
        val existing2 = allCycles.find { it.name == "Existing 2" }
        assertThat(existing1!!.position).isEqualTo(1)
        assertThat(existing2!!.position).isEqualTo(2)
    }

    @Test
    fun `sortCyclesByDateUsed sorts cycles by dateLastLogged descending`() = runTest {
        val cycle1Id = createTestCycle("Cycle 1")
        val cycle2Id = createTestCycle("Cycle 2")
        val cycle3Id = createTestCycle("Cycle 3")

        // Set different dateLastLogged values
        db.cycleDao().updateTrainingDate(cycle1Id, 1000L)
        db.cycleDao().updateTrainingDate(cycle2Id, 3000L)
        db.cycleDao().updateTrainingDate(cycle3Id, 2000L)

        trackerLocalDataSource.sortCyclesByDateUsed()

        val sortedCycles = db.cycleDao().getCycles()
        assertThat(sortedCycles[0].name).isEqualTo("Cycle 2") // Most recent
        assertThat(sortedCycles[1].name).isEqualTo("Cycle 3") // Middle
        assertThat(sortedCycles[2].name).isEqualTo("Cycle 1") // Oldest
    }

    // WORKOUT OPERATIONS TESTS
    @Test
    fun `observeWorkouts returns LiveData with workouts for cycle`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId, "Test Workout")

        val workoutsLiveData = trackerLocalDataSource.observeWorkouts(cycleId)
        workoutsLiveData.getOrAwaitValue()

        val result = workoutsLiveData.value
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val workouts = (result as Result.Success).data
        assertThat(workouts).hasSize(1)
        assertThat(workouts[0].name).isEqualTo("Test Workout")
    }

    @Test
    fun `insertWorkout adds workout to database`() = runTest {
        val cycleId = createTestCycle()
        val workout = Workout.createDefault("New Workout", 3, 0).apply { this.cycleId = cycleId }

        trackerLocalDataSource.insertWorkout(workout)

        val workouts = db.workoutDao().getWorkoutsSync(cycleId)
        assertThat(workouts).hasSize(1)
        assertThat(workouts[0].name).isEqualTo("New Workout")
    }

    @Test
    fun `deleteWorkout removes workout and reorders positions`() = runTest {
        val cycleId = createTestCycle()
        val workout1Id = createTestWorkout(cycleId, "Workout 1", 0)
        val workout2Id = createTestWorkout(cycleId, "Workout 2", 1)
        val workout3Id = createTestWorkout(cycleId, "Workout 3", 2)

        val workoutToDelete = db.workoutDao().getWorkoutById(workout2Id)!!
        trackerLocalDataSource.deleteWorkout(workoutToDelete)

        val remainingWorkouts = db.workoutDao().getWorkoutsSync(cycleId)
        assertThat(remainingWorkouts).hasSize(2)
        assertThat(remainingWorkouts[0].name).isEqualTo("Workout 1")
        assertThat(remainingWorkouts[0].position).isEqualTo(0)
        assertThat(remainingWorkouts[1].name).isEqualTo("Workout 3")
        assertThat(remainingWorkouts[1].position).isEqualTo(1) // Reordered
    }

    @Test
    fun `updateWorkoutName updates workout name`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId, "Original Name")
        val workout = db.workoutDao().getWorkoutById(workoutId)!!

        trackerLocalDataSource.updateWorkoutName(workout, "New Name")

        val updatedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(updatedWorkout!!.name).isEqualTo("New Name")
    }

    @Test
    fun `saveNewWorkoutPositions updates all workout positions`() = runTest {
        val cycleId = createTestCycle()
        val workout1Id = createTestWorkout(cycleId, "Workout 1", 0)
        val workout2Id = createTestWorkout(cycleId, "Workout 2", 1)
        val workout3Id = createTestWorkout(cycleId, "Workout 3", 2)

        val workouts = db.workoutDao().getWorkoutsSync(cycleId)
        // Reverse the order
        val reorderedWorkouts = workouts.reversed()

        trackerLocalDataSource.saveNewWorkoutPositions(reorderedWorkouts)

        val updatedWorkouts = db.workoutDao().getWorkoutsSync(cycleId)
        assertThat(updatedWorkouts[0].name).isEqualTo("Workout 3")
        assertThat(updatedWorkouts[1].name).isEqualTo("Workout 2")
        assertThat(updatedWorkouts[2].name).isEqualTo("Workout 1")
    }

    // EXERCISE SET OPERATIONS TESTS
    @Test
    fun `insertSet adds exercise set to database`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val exerciseSet = ExerciseSet.createDefault("New Exercise", 6, 10, 120).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 0
        }

        trackerLocalDataSource.insertSet(exerciseSet)

        val sets = db.setDao().getExerciseSetsSync(cycleId, workoutId)
        assertThat(sets).hasSize(1)
        assertThat(sets[0].exerciseName).isEqualTo("New Exercise")
    }

    @Test
    fun `getExerciseSets returns LiveData with exercise sets`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Test Exercise")

        val setsLiveData = trackerLocalDataSource.getExerciseSets(cycleId, workoutId)
        setsLiveData.getOrAwaitValue()

        val sets = setsLiveData.value
        assertThat(sets).isNotNull()
        assertThat(sets).hasSize(1)
        assertThat(sets!![0].exerciseName).isEqualTo("Test Exercise")
    }

    @Test
    fun `getExerciseSetsSync returns exercise sets synchronously`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Sync Exercise")

        val sets = trackerLocalDataSource.getExerciseSetsSync(cycleId, workoutId)

        assertThat(sets).hasSize(1)
        assertThat(sets[0].exerciseName).isEqualTo("Sync Exercise")
    }

    @Test
    fun `updateExerciseSet updates exercise set`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Original Exercise")
        val originalSet = db.setDao().getExerciseSet(setId)!!

        val updatedSet = originalSet.copy(exerciseName = "Updated Exercise", lowerReps = 10)
        trackerLocalDataSource.updateExerciseSet(updatedSet)

        val retrievedSet = db.setDao().getExerciseSet(setId)
        assertThat(retrievedSet!!.exerciseName).isEqualTo("Updated Exercise")
        assertThat(retrievedSet.lowerReps).isEqualTo(10)
    }

    @Test
    fun `deleteExerciseSet removes set and reorders positions`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val set1Id = createTestExerciseSet(cycleId, workoutId, "Exercise 1", 0)
        val set2Id = createTestExerciseSet(cycleId, workoutId, "Exercise 2", 1)
        val set3Id = createTestExerciseSet(cycleId, workoutId, "Exercise 3", 2)

        val setToDelete = db.setDao().getExerciseSet(set2Id)!!
        trackerLocalDataSource.deleteExerciseSet(setToDelete)

        val remainingSets = db.setDao().getExerciseSetsSync(cycleId, workoutId)
        assertThat(remainingSets).hasSize(2)
        assertThat(remainingSets[0].exerciseName).isEqualTo("Exercise 1")
        assertThat(remainingSets[0].position).isEqualTo(0)
        assertThat(remainingSets[1].exerciseName).isEqualTo("Exercise 3")
        assertThat(remainingSets[1].position).isEqualTo(1) // Reordered
    }

    @Test
    fun `addExerciseSets adds multiple sets and reorders existing positions`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        // Create existing sets
        createTestExerciseSet(cycleId, workoutId, "Existing 1", 0)
        createTestExerciseSet(cycleId, workoutId, "Existing 2", 1)

        trackerLocalDataSource.addExerciseSets(
            position = 1,
            numSets = 2,
            cycleId = cycleId,
            workoutId = workoutId,
            exerciseName = "New Exercise",
            lowerReps = 8,
            higherReps = 12,
            restTime = 90
        )

        val allSets = db.setDao().getExerciseSetsSync(cycleId, workoutId)
        assertThat(allSets).hasSize(4)

        // Verify new sets were inserted at correct positions
        val newSets = allSets.filter { it.exerciseName == "New Exercise" }
        assertThat(newSets).hasSize(2)
        assertThat(newSets[0].position).isEqualTo(1)
        assertThat(newSets[1].position).isEqualTo(2)

        // Verify existing sets were repositioned
        val existingSet2 = allSets.find { it.exerciseName == "Existing 2" }
        assertThat(existingSet2!!.position).isEqualTo(3) // Shifted by numSets
    }

    @Test
    fun `updateRepRange updates both min and max reps`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Rep Test")

        trackerLocalDataSource.updateRepRange(setId, 6, 15)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.lowerReps).isEqualTo(6)
        assertThat(updatedSet.higherReps).isEqualTo(15)
    }

    @Test
    fun `updateRestTime updates rest time`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Rest Test")

        trackerLocalDataSource.updateRestTime(setId, 120)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.restTime).isEqualTo(120)
    }

    @Test
    fun `updateExerciseName updates exercise name`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Original Name")

        trackerLocalDataSource.updateExerciseName(setId, "New Exercise Name")

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.exerciseName).isEqualTo("New Exercise Name")
    }

    // EXERCISE LOG OPERATIONS TESTS
    @Test
    fun `updateReps creates new log if not exists`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        trackerLocalDataSource.updateReps(setId, 1, 10)

        val log = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(log).isNotNull()
        assertThat(log!!.reps).isEqualTo(10)
        assertThat(log.setId).isEqualTo(setId)
        assertThat(log.day).isEqualTo(1)
    }

    @Test
    fun `updateReps updates existing log`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        // Create initial log
        val initialLog = ExerciseLog.createDefault(setId, 1).apply { reps = 8 }
        db.exerciseLogDao().insertLog(initialLog)

        trackerLocalDataSource.updateReps(setId, 1, 12)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.reps).isEqualTo(12)
    }

    @Test
    fun `updateWeight creates new log if not exists`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        trackerLocalDataSource.updateWeight(setId, 1, 135.5)

        val log = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(log).isNotNull()
        assertThat(log!!.weight).isEqualTo(135.5)
    }

    @Test
    fun `updateWeight updates existing log`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        // Create initial log
        val initialLog = ExerciseLog.createDefault(setId, 1).apply { weight = 100.0 }
        db.exerciseLogDao().insertLog(initialLog)

        trackerLocalDataSource.updateWeight(setId, 1, 125.0)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.weight).isEqualTo(125.0)
    }

    @Test
    fun `changeLogExerciseName creates substitute exercise log`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Original Exercise")

        trackerLocalDataSource.changeLogExerciseName(setId, 1, "Substitute Exercise")

        val log = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(log).isNotNull()
        assertThat(log!!.subName).isEqualTo("Substitute Exercise")
    }

    @Test
    fun `changeLogExerciseName removes substitution when name matches original`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId, "Original Exercise")

        // Create log with substitution
        val log = ExerciseLog.createDefault(setId, 1).apply { subName = "Old Sub" }
        db.exerciseLogDao().insertLog(log)

        trackerLocalDataSource.changeLogExerciseName(setId, 1, "Original Exercise")

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.subName).isNull()
    }

    @Test
    fun `skipExercise marks exercise as skipped`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        trackerLocalDataSource.skipExercise(setId, 1)

        val log = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(log).isNotNull()
        assertThat(log!!.skip).isTrue()
    }

    @Test
    fun `undoSkipExercise marks exercise as not skipped`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val setId = createTestExerciseSet(cycleId, workoutId)

        // First skip the exercise
        trackerLocalDataSource.skipExercise(setId, 1)

        trackerLocalDataSource.undoSkipExercise(setId, 1)

        val log = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(log!!.skip).isFalse()
    }

    @Test
    fun `skipDay marks all exercises in workout as skipped`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val set1Id = createTestExerciseSet(cycleId, workoutId, "Exercise 1", 0)
        val set2Id = createTestExerciseSet(cycleId, workoutId, "Exercise 2", 1)

        trackerLocalDataSource.skipDay(cycleId, workoutId, 1)

        val log1 = db.exerciseLogDao().getExerciseLog(set1Id, 1)
        val log2 = db.exerciseLogDao().getExerciseLog(set2Id, 1)
        assertThat(log1!!.skip).isTrue()
        assertThat(log2!!.skip).isTrue()
    }

    @Test
    fun `saveTrainingDate updates cycle training date`() = runTest {
        val cycleId = createTestCycle()

        trackerLocalDataSource.saveTrainingDate(cycleId)

        val updatedCycle = db.cycleDao().getCycleById(cycleId)
        assertThat(updatedCycle!!.dateLastLogged).isGreaterThan(0L)
    }

    @Test
    fun `getNextExerciseName returns next exercise name`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val set1Id = createTestExerciseSet(cycleId, workoutId, "Exercise 1", 0)
        val set2Id = createTestExerciseSet(cycleId, workoutId, "Exercise 2", 1)

        val result = trackerLocalDataSource.getNextExerciseName(set1Id, 1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val nextName = (result as Result.Success).data
        assertThat(nextName).isEqualTo("Exercise 2")
    }

    @Test
    fun `getNextExerciseName returns substituted name when exists`() = runTest {
        val cycleId = createTestCycle()
        val workoutId = createTestWorkout(cycleId)
        val set1Id = createTestExerciseSet(cycleId, workoutId, "Exercise 1", 0)
        val set2Id = createTestExerciseSet(cycleId, workoutId, "Exercise 2", 1)

        // Create substituted log for next exercise
        val subLog = ExerciseLog.createDefault(set2Id, 1).apply { subName = "Substitute Exercise" }
        db.exerciseLogDao().insertLog(subLog)

        val result = trackerLocalDataSource.getNextExerciseName(set1Id, 1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val nextName = (result as Result.Success).data
        assertThat(nextName).isEqualTo("Substitute Exercise")
    }

    // USER EXERCISE OPERATIONS TESTS
    @Test
    fun `addExerciseNameToBank adds exercise to bank`() = runTest {
        trackerLocalDataSource.addExerciseNameToBank("Custom Exercise")

        val userExercise = db.userExerciseDao().getUserExercise("Custom Exercise")
        assertThat(userExercise).isNotNull()
        assertThat(userExercise!!.name).isEqualTo("Custom Exercise")
    }

    @Test
    fun `isExerciseNameInBank returns true when exercise exists`() = runTest {
        db.userExerciseDao().insertExercise(UserExercise(0L, "Existing Exercise"))

        val exists = trackerLocalDataSource.isExerciseNameInBank("Existing Exercise")

        assertThat(exists).isTrue()
    }

    @Test
    fun `isExerciseNameInBank returns false when exercise does not exist`() = runTest {
        val exists = trackerLocalDataSource.isExerciseNameInBank("Non-existent Exercise")

        assertThat(exists).isFalse()
    }

    @Test
    fun `getAllUserExercises returns all user exercises`() = runTest {
        db.userExerciseDao().insertExercise(UserExercise(0L, "Exercise 1"))
        db.userExerciseDao().insertExercise(UserExercise(0L, "Exercise 2"))

        val exercises = trackerLocalDataSource.getAllUserExercises()

        assertThat(exercises).hasSize(2)
        assertThat(exercises.map { it.name }).containsExactly("Exercise 1", "Exercise 2")
    }

    @Test
    fun `deleteUserExercise removes exercise from bank`() = runTest {
        db.userExerciseDao().insertExercise(UserExercise(0L, "To Delete"))

        trackerLocalDataSource.deleteUserExercise("To Delete")

        val deletedExercise = db.userExerciseDao().getUserExercise("To Delete")
        assertThat(deletedExercise).isNull()
    }

    // COMPLEX WORKFLOW TESTS
    @Test
    fun `createNestedCycle creates complete cycle with workouts and sets`() = runTest {
        val cycle = Cycle.createDefault("Nested Cycle", 8)
        val workout = Workout.createDefault("Nested Workout", 3, 0)
        val exerciseSet = ExerciseSet.createDefault("Nested Exercise", 8, 12, 90)

        val nestedCycle: CycleMap = hashMapOf(
            cycle to listOf(
                hashMapOf(workout to listOf(exerciseSet))
            )
        )

        trackerLocalDataSource.createNestedCycle(nestedCycle)

        val cycles = db.cycleDao().getCycles()
        assertThat(cycles).hasSize(1)
        assertThat(cycles[0].name).isEqualTo("Nested Cycle")

        val workouts = db.workoutDao().getWorkoutsSync(cycles[0].id)
        assertThat(workouts).hasSize(1)
        assertThat(workouts[0].name).isEqualTo("Nested Workout")

        val sets = db.setDao().getExerciseSetsSync(cycles[0].id, workouts[0].id)
        assertThat(sets).hasSize(1)
        assertThat(sets[0].exerciseName).isEqualTo("Nested Exercise")
    }

    @Test
    fun `getTrainingPagerData returns cycle and workout pair`() = runTest {
        val cycleId = createTestCycle("Pager Cycle")
        val workoutId = createTestWorkout(cycleId, "Pager Workout")

        val result = trackerLocalDataSource.getTrainingPagerData(cycleId, workoutId)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val pagerData = (result as Result.Success).data
        assertThat(pagerData.first.name).isEqualTo("Pager Cycle")
        assertThat(pagerData.second.name).isEqualTo("Pager Workout")
    }

    @Test
    fun `getTrainingPagerData returns error when cycle not found`() = runTest {
        val workoutId = createTestWorkout(createTestCycle(), "Workout")

        val result = trackerLocalDataSource.getTrainingPagerData(999L, workoutId)

        assertThat(result).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun `cloneCycle creates complete copy of cycle with all data`() = runTest {
        val originalCycleId = createTestCycle("Original Cycle")
        val workoutId = createTestWorkout(originalCycleId, "Original Workout")
        val setId = createTestExerciseSet(originalCycleId, workoutId, "Original Exercise")

        // Add some logs and notes
        val log = ExerciseLog.createDefault(setId, 1).apply {
            weight = 100.0
            reps = 10
        }
        db.exerciseLogDao().insertLog(log)

        val note = ExerciseNote(setId, 1, "Test note", System.currentTimeMillis())
        db.exerciseNoteDao().insertExerciseNote(note)

        trackerLocalDataSource.cloneCycle(originalCycleId)

        val allCycles = db.cycleDao().getCycles()
        assertThat(allCycles).hasSize(2)

        val clonedCycle = allCycles.find { it.id != originalCycleId }
        assertThat(clonedCycle).isNotNull()
        assertThat(clonedCycle!!.name).isEqualTo("Original Cycle")
        assertThat(clonedCycle.dateLastLogged).isEqualTo(0L) // Reset for clone

        val clonedWorkouts = db.workoutDao().getWorkoutsSync(clonedCycle.id)
        assertThat(clonedWorkouts).hasSize(1)
        assertThat(clonedWorkouts[0].name).isEqualTo("Original Workout")

        val clonedSets = db.setDao().getExerciseSetsSync(clonedCycle.id, clonedWorkouts[0].id)
        assertThat(clonedSets).hasSize(1)
        assertThat(clonedSets[0].exerciseName).isEqualTo("Original Exercise")

        val clonedLogs = db.exerciseLogDao().getExerciseLogs(clonedSets[0].id)
        assertThat(clonedLogs).hasSize(1)
        assertThat(clonedLogs[0].weight).isEqualTo(100.0)

        val clonedNotes = db.exerciseNoteDao().getExerciseNotes(clonedSets[0].id)
        assertThat(clonedNotes).hasSize(1)
        assertThat(clonedNotes[0].note).isEqualTo("Test note")
    }

    @Test
    fun `complete workout logging workflow works correctly`() = runTest {
        // Create complete hierarchy
        val cycleId = createTestCycle("Workout Cycle")
        val workoutId = createTestWorkout(cycleId, "Push Day")
        val set1Id = createTestExerciseSet(cycleId, workoutId, "Bench Press", 0)
        val set2Id = createTestExerciseSet(cycleId, workoutId, "Shoulder Press", 1)

        // Log workout data
        trackerLocalDataSource.updateWeight(set1Id, 1, 135.0)
        trackerLocalDataSource.updateReps(set1Id, 1, 8)
        trackerLocalDataSource.updateWeight(set2Id, 1, 75.0)
        trackerLocalDataSource.updateReps(set2Id, 1, 10)

        // Add a note
        trackerLocalDataSource.insertNote(set1Id, 1, "Felt strong today!")

        // Save training date
        trackerLocalDataSource.saveTrainingDate(cycleId)

        // Verify all data was logged correctly
        val set1Log = db.exerciseLogDao().getExerciseLog(set1Id, 1)
        assertThat(set1Log!!.weight).isEqualTo(135.0)
        assertThat(set1Log.reps).isEqualTo(8)
        assertThat(set1Log.hasNote).isTrue()

        val set2Log = db.exerciseLogDao().getExerciseLog(set2Id, 1)
        assertThat(set2Log!!.weight).isEqualTo(75.0)
        assertThat(set2Log.reps).isEqualTo(10)

        val note = db.exerciseNoteDao().getExerciseNote(set1Id, 1)
        assertThat(note!!.note).isEqualTo("Felt strong today!")

        val updatedCycle = db.cycleDao().getCycleById(cycleId)
        assertThat(updatedCycle!!.dateLastLogged).isGreaterThan(0L)
    }
}
