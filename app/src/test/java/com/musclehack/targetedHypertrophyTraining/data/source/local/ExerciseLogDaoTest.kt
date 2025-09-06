package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.musclehack.targetedHypertrophyTraining.MainCoroutineRule
import com.musclehack.targetedHypertrophyTraining.getOrAwaitValue
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseLog
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ExerciseLogDaoTest {

    private lateinit var db: AppDatabase

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
    }

    @After
    fun closeDb() = db.close()

    private suspend fun createTestHierarchy(): Triple<Long, Long, Long> {
        // Create cycle -> workout -> exerciseSet hierarchy
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Test Workout", 3, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(workout)
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)
        return Triple(cycleId, workoutId, setId)
    }

    @Test
    fun `insertLog inserts exercise log correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            weight = 135.0
            reps = 10
            logDate = System.currentTimeMillis()
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog!!.setId).isEqualTo(setId)
        assertThat(retrievedLog.day).isEqualTo(1)
        assertThat(retrievedLog.weight).isEqualTo(135.0)
        assertThat(retrievedLog.reps).isEqualTo(10)
        assertThat(retrievedLog.logDate).isNotNull()
        assertThat(retrievedLog.hasNote).isFalse()
        assertThat(retrievedLog.skip).isFalse()
    }

    @Test
    fun `insertLogSync inserts exercise log correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 2).apply {
            weight = 225.5
            reps = 8
            hasNote = true
            subName = "Substitute Exercise"
        }

        db.exerciseLogDao().insertLogSync(exerciseLog)

        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 2)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog!!.weight).isEqualTo(225.5)
        assertThat(retrievedLog.reps).isEqualTo(8)
        assertThat(retrievedLog.hasNote).isTrue()
        assertThat(retrievedLog.subName).isEqualTo("Substitute Exercise")
    }

    @Test
    fun `getAllExerciseLogs returns all logs`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        val log1 = ExerciseLog.createDefault(setId1, 1).apply { weight = 100.0; reps = 12 }
        val log2 = ExerciseLog.createDefault(setId1, 2).apply { weight = 105.0; reps = 10 }
        val log3 = ExerciseLog.createDefault(setId2, 1).apply { weight = 200.0; reps = 8 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)
        db.exerciseLogDao().insertLog(log3)

        val allLogs = db.exerciseLogDao().getAllExerciseLogs()

        assertThat(allLogs).hasSize(3)
        val logWeights = allLogs.map { it.weight }
        assertThat(logWeights).containsExactly(100.0, 105.0, 200.0)
    }

    @Test
    fun `getExerciseLogs returns LiveData with all logs`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        val log1 = ExerciseLog.createDefault(setId, 1).apply { weight = 100.0 }
        val log2 = ExerciseLog.createDefault(setId, 2).apply { weight = 105.0 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)

        val logsLiveData = db.exerciseLogDao().getExerciseLogs()
        logsLiveData.getOrAwaitValue()

        val logs = logsLiveData.value
        assertThat(logs).isNotNull()
        assertThat(logs).hasSize(2)
    }

    @Test
    fun `getExerciseLogs with setIds and day returns filtered logs`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        val log1 = ExerciseLog.createDefault(setId1, 1).apply { weight = 100.0 }
        val log2 = ExerciseLog.createDefault(setId1, 2).apply { weight = 105.0 }
        val log3 = ExerciseLog.createDefault(setId2, 1).apply { weight = 200.0 }
        val log4 = ExerciseLog.createDefault(setId2, 2).apply { weight = 205.0 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)
        db.exerciseLogDao().insertLog(log3)
        db.exerciseLogDao().insertLog(log4)

        val filteredLogsLiveData = db.exerciseLogDao().getExerciseLogs(listOf(setId1, setId2), 1)
        filteredLogsLiveData.getOrAwaitValue()

        val filteredLogs = filteredLogsLiveData.value
        assertThat(filteredLogs).isNotNull()
        assertThat(filteredLogs).hasSize(2)
        val weights = filteredLogs!!.map { it.weight }
        assertThat(weights).containsExactly(100.0, 200.0)
    }

    @Test
    fun `getExerciseLog returns specific log when exists`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 3).apply {
            weight = 155.5
            reps = 6
            hasNote = true
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 3)

        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog!!.setId).isEqualTo(setId)
        assertThat(retrievedLog.day).isEqualTo(3)
        assertThat(retrievedLog.weight).isEqualTo(155.5)
        assertThat(retrievedLog.reps).isEqualTo(6)
        assertThat(retrievedLog.hasNote).isTrue()
    }

    @Test
    fun `getExerciseLog returns null when log does not exist`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 999)

        assertThat(retrievedLog).isNull()
    }

    @Test
    fun `getExerciseLogs for specific setId returns all logs for that set`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        val log1 = ExerciseLog.createDefault(setId1, 1).apply { weight = 100.0 }
        val log2 = ExerciseLog.createDefault(setId1, 2).apply { weight = 105.0 }
        val log3 = ExerciseLog.createDefault(setId1, 3).apply { weight = 110.0 }
        val log4 = ExerciseLog.createDefault(setId2, 1).apply { weight = 200.0 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)
        db.exerciseLogDao().insertLog(log3)
        db.exerciseLogDao().insertLog(log4)

        val set1Logs = db.exerciseLogDao().getExerciseLogs(setId1)

        assertThat(set1Logs).hasSize(3)
        val weights = set1Logs.map { it.weight }
        assertThat(weights).containsExactly(100.0, 105.0, 110.0)
    }

    @Test
    fun `update updates exercise log correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val originalLog = ExerciseLog.createDefault(setId, 1).apply {
            weight = 100.0
            reps = 10
        }

        db.exerciseLogDao().insertLog(originalLog)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)!!.copy(
            weight = 120.0,
            reps = 8,
            hasNote = true
        )

        db.exerciseLogDao().update(updatedLog)

        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(retrievedLog!!.weight).isEqualTo(120.0)
        assertThat(retrievedLog.reps).isEqualTo(8)
        assertThat(retrievedLog.hasNote).isTrue()
        assertThat(retrievedLog.setId).isEqualTo(setId) // Primary key maintained
        assertThat(retrievedLog.day).isEqualTo(1) // Primary key maintained
    }

    @Test
    fun `getExerciseLogBeforeDay returns logs before specified day ordered by day desc`() =
        runTest {
            val (_, _, setId) = createTestHierarchy()

            val log1 = ExerciseLog.createDefault(setId, 1).apply { weight = 100.0 }
            val log2 = ExerciseLog.createDefault(setId, 3).apply { weight = 105.0 }
            val log3 = ExerciseLog.createDefault(setId, 5).apply { weight = 110.0 }
            val log4 = ExerciseLog.createDefault(setId, 7).apply { weight = 115.0 }

            db.exerciseLogDao().insertLog(log1)
            db.exerciseLogDao().insertLog(log2)
            db.exerciseLogDao().insertLog(log3)
            db.exerciseLogDao().insertLog(log4)

            val logsBefore = db.exerciseLogDao().getExerciseLogBeforeDay(6, setId)

            assertThat(logsBefore).hasSize(3)
            // Should be ordered by day DESC
            assertThat(logsBefore[0].day).isEqualTo(5)
            assertThat(logsBefore[1].day).isEqualTo(3)
            assertThat(logsBefore[2].day).isEqualTo(1)
        }

    @Test
    fun `observeExerciseLogBeforeDay returns LiveData with logs before specified day`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        val log1 = ExerciseLog.createDefault(setId, 2).apply { weight = 100.0 }
        val log2 = ExerciseLog.createDefault(setId, 4).apply { weight = 105.0 }
        val log3 = ExerciseLog.createDefault(setId, 6).apply { weight = 110.0 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)
        db.exerciseLogDao().insertLog(log3)

        val logsBeforeLiveData = db.exerciseLogDao().observeExerciseLogBeforeDay(5, setId)
        logsBeforeLiveData.getOrAwaitValue()

        val logsBefore = logsBeforeLiveData.value
        assertThat(logsBefore).isNotNull()
        assertThat(logsBefore).hasSize(2)
        // Should be ordered by day DESC
        assertThat(logsBefore!![0].day).isEqualTo(4)
        assertThat(logsBefore[1].day).isEqualTo(2)
    }

    @Test
    fun `getExerciseLogsBeforeAndIncludingDay returns logs up to and including specified day`() =
        runTest {
            val (_, _, setId) = createTestHierarchy()

            val log1 = ExerciseLog.createDefault(setId, 1).apply { weight = 100.0 }
            val log2 = ExerciseLog.createDefault(setId, 3).apply { weight = 105.0 }
            val log3 = ExerciseLog.createDefault(setId, 5).apply { weight = 110.0 }
            val log4 = ExerciseLog.createDefault(setId, 7).apply { weight = 115.0 }

            db.exerciseLogDao().insertLog(log1)
            db.exerciseLogDao().insertLog(log2)
            db.exerciseLogDao().insertLog(log3)
            db.exerciseLogDao().insertLog(log4)

            val logsUpTo = db.exerciseLogDao().getExerciseLogsBeforeAndIncludingDay(5, setId)

            assertThat(logsUpTo).hasSize(3)
            // Should be ordered by day DESC and include day 5
            assertThat(logsUpTo[0].day).isEqualTo(5)
            assertThat(logsUpTo[1].day).isEqualTo(3)
            assertThat(logsUpTo[2].day).isEqualTo(1)
        }

    @Test
    fun `updateWeight updates only weight field and returns count`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            weight = 100.0
            reps = 10
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val updateCount = db.exerciseLogDao().updateWeight(setId, 1, 125.5)

        assertThat(updateCount).isEqualTo(1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.weight).isEqualTo(125.5)
        assertThat(updatedLog.reps).isEqualTo(10) // Should remain unchanged
    }

    @Test
    fun `updateReps updates only reps field and returns count`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            weight = 100.0
            reps = 10
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val updateCount = db.exerciseLogDao().updateReps(setId, 1, 12)

        assertThat(updateCount).isEqualTo(1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.reps).isEqualTo(12)
        assertThat(updatedLog.weight).isEqualTo(100.0) // Should remain unchanged
    }

    @Test
    fun `updateSkip updates only skip field and returns count`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            skip = false
            weight = 100.0
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val updateCount = db.exerciseLogDao().updateSkip(setId, 1, true)

        assertThat(updateCount).isEqualTo(1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.skip).isTrue()
        assertThat(updatedLog.weight).isEqualTo(100.0) // Should remain unchanged
    }

    @Test
    fun `updateNoteStatus updates only hasNote field and returns count`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            hasNote = false
            weight = 100.0
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        val updateCount = db.exerciseLogDao().updateNoteStatus(setId, 1, true)

        assertThat(updateCount).isEqualTo(1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.hasNote).isTrue()
        assertThat(updatedLog.weight).isEqualTo(100.0) // Should remain unchanged
    }

    @Test
    fun `updateSubstitutionSingleDay updates only subName field`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            subName = null
            weight = 100.0
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        db.exerciseLogDao().updateSubstitutionSingleDay("Substitute Exercise", setId, 1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.subName).isEqualTo("Substitute Exercise")
        assertThat(updatedLog.weight).isEqualTo(100.0) // Should remain unchanged
    }

    @Test
    fun `insertLog with replace strategy replaces existing log`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val log1 = ExerciseLog(setId, 1, null, 100.0, 10, false, null, false)
        val log2 = ExerciseLog(setId, 1, null, 150.0, 8, true, "Replaced", false)

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2) // Should replace due to OnConflictStrategy.REPLACE

        val allLogs = db.exerciseLogDao().getAllExerciseLogs()
        assertThat(allLogs).hasSize(1)
        assertThat(allLogs[0].weight).isEqualTo(150.0)
        assertThat(allLogs[0].reps).isEqualTo(8)
        assertThat(allLogs[0].hasNote).isTrue()
        assertThat(allLogs[0].subName).isEqualTo("Replaced")
    }

    @Test
    fun `foreign key cascade delete removes logs when exercise set is deleted`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseSet = db.setDao().getExerciseSet(setId)!!

        val log1 = ExerciseLog.createDefault(setId, 1).apply { weight = 100.0 }
        val log2 = ExerciseLog.createDefault(setId, 2).apply { weight = 105.0 }

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2)

        // Verify logs exist
        val logsBeforeDelete = db.exerciseLogDao().getExerciseLogs(setId)
        assertThat(logsBeforeDelete).hasSize(2)

        // Delete the exercise set
        db.setDao().deleteExerciseSet(exerciseSet)

        // Verify logs are cascade deleted
        val logsAfterDelete = db.exerciseLogDao().getAllExerciseLogs()
        assertThat(logsAfterDelete).isEmpty()
    }

    @Test
    fun `composite primary key prevents duplicate setId and day combinations`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val log1 = ExerciseLog(setId, 1, null, 100.0, 10, false, null, false)
        val log2 = ExerciseLog(setId, 1, null, 150.0, 8, true, null, false) // Same setId and day

        db.exerciseLogDao().insertLog(log1)
        db.exerciseLogDao().insertLog(log2) // Should replace due to composite primary key

        val allLogs = db.exerciseLogDao().getAllExerciseLogs()
        assertThat(allLogs).hasSize(1)
        assertThat(allLogs[0].weight).isEqualTo(150.0) // Should be the second log
    }

    @Test
    fun `multiple sets with logs work independently`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        val set1Log1 = ExerciseLog.createDefault(setId1, 1).apply { weight = 100.0 }
        val set1Log2 = ExerciseLog.createDefault(setId1, 2).apply { weight = 105.0 }
        val set2Log1 = ExerciseLog.createDefault(setId2, 1).apply { weight = 200.0 }

        db.exerciseLogDao().insertLog(set1Log1)
        db.exerciseLogDao().insertLog(set1Log2)
        db.exerciseLogDao().insertLog(set2Log1)

        val set1Logs = db.exerciseLogDao().getExerciseLogs(setId1)
        val set2Logs = db.exerciseLogDao().getExerciseLogs(setId2)
        val allLogs = db.exerciseLogDao().getAllExerciseLogs()

        assertThat(set1Logs).hasSize(2)
        assertThat(set2Logs).hasSize(1)
        assertThat(allLogs).hasSize(3)

        assertThat(set1Logs.map { it.weight }).containsExactly(100.0, 105.0)
        assertThat(set2Logs[0].weight).isEqualTo(200.0)
    }

    @Test
    fun `exercise log properties are preserved correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val logDate = System.currentTimeMillis()
        val exerciseLog = ExerciseLog(
            setId = setId,
            day = 5,
            logDate = logDate,
            weight = 185.5,
            reps = 6,
            hasNote = true,
            subName = "Substitute Exercise",
            skip = true
        )

        db.exerciseLogDao().insertLog(exerciseLog)
        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 5)

        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog!!.setId).isEqualTo(setId)
        assertThat(retrievedLog.day).isEqualTo(5)
        assertThat(retrievedLog.logDate).isEqualTo(logDate)
        assertThat(retrievedLog.weight).isEqualTo(185.5)
        assertThat(retrievedLog.reps).isEqualTo(6)
        assertThat(retrievedLog.hasNote).isTrue()
        assertThat(retrievedLog.subName).isEqualTo("Substitute Exercise")
        assertThat(retrievedLog.skip).isTrue()
    }

    @Test
    fun `multiple field updates work correctly together`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply {
            weight = 100.0
            reps = 10
            hasNote = false
            skip = false
        }

        db.exerciseLogDao().insertLog(exerciseLog)

        // Update multiple fields
        db.exerciseLogDao().updateWeight(setId, 1, 125.0)
        db.exerciseLogDao().updateReps(setId, 1, 8)
        db.exerciseLogDao().updateNoteStatus(setId, 1, true)
        db.exerciseLogDao().updateSkip(setId, 1, true)
        db.exerciseLogDao().updateSubstitutionSingleDay("New Sub", setId, 1)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.weight).isEqualTo(125.0)
        assertThat(updatedLog.reps).isEqualTo(8)
        assertThat(updatedLog.hasNote).isTrue()
        assertThat(updatedLog.skip).isTrue()
        assertThat(updatedLog.subName).isEqualTo("New Sub")
    }

    @Test
    fun `null values are handled correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseLog = ExerciseLog(
            setId = setId,
            day = 1,
            logDate = null,
            weight = null,
            reps = null,
            hasNote = false,
            subName = null,
            skip = false
        )

        db.exerciseLogDao().insertLog(exerciseLog)
        val retrievedLog = db.exerciseLogDao().getExerciseLog(setId, 1)

        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog!!.logDate).isNull()
        assertThat(retrievedLog.weight).isNull()
        assertThat(retrievedLog.reps).isNull()
        assertThat(retrievedLog.subName).isNull()

        // Test updating null values
        db.exerciseLogDao().updateWeight(setId, 1, null)
        db.exerciseLogDao().updateReps(setId, 1, null)

        val updatedLog = db.exerciseLogDao().getExerciseLog(setId, 1)
        assertThat(updatedLog!!.weight).isNull()
        assertThat(updatedLog.reps).isNull()
    }
}
