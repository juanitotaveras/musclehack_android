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
class SetDaoTest {

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
        return Triple(cycleId, workoutId, workoutId)
    }

    @Test
    fun `insertSet inserts exercise set correctly`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Bench Press", 8, 12, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }

        db.setDao().insertSet(exerciseSet)

        val retrievedSets = db.setDao().getExerciseSetsSync(cycleId, workoutId)
        assertThat(retrievedSets).hasSize(1)

        val retrievedSet = retrievedSets[0]
        assertThat(retrievedSet.exerciseName).isEqualTo("Bench Press")
        assertThat(retrievedSet.lowerReps).isEqualTo(8)
        assertThat(retrievedSet.higherReps).isEqualTo(12)
        assertThat(retrievedSet.restTime).isEqualTo(90)
        assertThat(retrievedSet.position).isEqualTo(1)
        assertThat(retrievedSet.cycleId).isEqualTo(cycleId)
        assertThat(retrievedSet.workoutId).isEqualTo(workoutId)
    }

    @Test
    fun `insertSetSync inserts exercise set and returns ID`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Squat", 6, 10, 120).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 2
        }

        val setId = db.setDao().insertSetSync(exerciseSet)

        assertThat(setId).isGreaterThan(0)

        val retrievedSet = db.setDao().getExerciseSet(setId)
        assertThat(retrievedSet).isNotNull()
        assertThat(retrievedSet!!.exerciseName).isEqualTo("Squat")
        assertThat(retrievedSet.lowerReps).isEqualTo(6)
        assertThat(retrievedSet.higherReps).isEqualTo(10)
        assertThat(retrievedSet.restTime).isEqualTo(120)
    }

    @Test
    fun `getAllExerciseSets returns all sets ordered by position`() = runTest {
        val (cycleId1, workoutId1, _) = createTestHierarchy()
        val (cycleId2, workoutId2, _) = createTestHierarchy()

        val set1 = ExerciseSet.createDefault("Exercise 1", 8, 12, 60).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 3
        }
        val set2 = ExerciseSet.createDefault("Exercise 2", 6, 10, 90).apply {
            this.cycleId = cycleId2
            this.workoutId = workoutId2
            this.position = 1
        }
        val set3 = ExerciseSet.createDefault("Exercise 3", 10, 15, 45).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 2
        }

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2)
        db.setDao().insertSet(set3)

        val allSets = db.setDao().getAllExerciseSets()

        assertThat(allSets).hasSize(3)
        assertThat(allSets[0].exerciseName).isEqualTo("Exercise 2") // position 1
        assertThat(allSets[1].exerciseName).isEqualTo("Exercise 3") // position 2
        assertThat(allSets[2].exerciseName).isEqualTo("Exercise 1") // position 3
    }

    @Test
    fun `getExerciseSets returns LiveData with sets for specific workout`() = runTest {
        val (cycleId1, workoutId1, _) = createTestHierarchy()
        val (cycleId2, workoutId2, _) = createTestHierarchy()

        val set1 = ExerciseSet.createDefault("Workout1 Exercise1", 8, 12, 60).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 2
        }
        val set2 = ExerciseSet.createDefault("Workout1 Exercise2", 6, 10, 90).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 1
        }
        val set3 = ExerciseSet.createDefault("Workout2 Exercise1", 10, 15, 45).apply {
            this.cycleId = cycleId2
            this.workoutId = workoutId2
            this.position = 1
        }

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2)
        db.setDao().insertSet(set3)

        val workout1SetsLiveData = db.setDao().getExerciseSets(cycleId1, workoutId1)
        workout1SetsLiveData.getOrAwaitValue()

        val workout1Sets = workout1SetsLiveData.value
        assertThat(workout1Sets).isNotNull()
        assertThat(workout1Sets).hasSize(2)
        assertThat(workout1Sets!![0].exerciseName).isEqualTo("Workout1 Exercise2") // position 1
        assertThat(workout1Sets[1].exerciseName).isEqualTo("Workout1 Exercise1") // position 2
    }

    @Test
    fun `getExerciseSetsSync returns sets for specific workout synchronously`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()

        val set1 = ExerciseSet.createDefault("Exercise 1", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 2
        }
        val set2 = ExerciseSet.createDefault("Exercise 2", 6, 10, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2)

        val sets = db.setDao().getExerciseSetsSync(cycleId, workoutId)

        assertThat(sets).hasSize(2)
        assertThat(sets[0].exerciseName).isEqualTo("Exercise 2") // position 1
        assertThat(sets[1].exerciseName).isEqualTo("Exercise 1") // position 2
    }

    @Test
    fun `getExerciseSet by ID returns specific set when exists`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Deadlift", 5, 8, 180).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }

        val setId = db.setDao().insertSetSync(exerciseSet)

        val retrievedSet = db.setDao().getExerciseSet(setId)

        assertThat(retrievedSet).isNotNull()
        assertThat(retrievedSet!!.id).isEqualTo(setId)
        assertThat(retrievedSet.exerciseName).isEqualTo("Deadlift")
        assertThat(retrievedSet.lowerReps).isEqualTo(5)
        assertThat(retrievedSet.higherReps).isEqualTo(8)
        assertThat(retrievedSet.restTime).isEqualTo(180)
    }

    @Test
    fun `getExerciseSet by ID returns null when set does not exist`() = runTest {
        val retrievedSet = db.setDao().getExerciseSet(999L)

        assertThat(retrievedSet).isNull()
    }

    @Test
    fun `getExerciseSet by position returns specific set in workout`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()

        val set1 = ExerciseSet.createDefault("Exercise 1", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val set2 = ExerciseSet.createDefault("Exercise 2", 6, 10, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 2
        }

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2)

        val retrievedSet = db.setDao().getExerciseSet(cycleId, workoutId, 2)

        assertThat(retrievedSet).isNotNull()
        assertThat(retrievedSet!!.exerciseName).isEqualTo("Exercise 2")
        assertThat(retrievedSet.position).isEqualTo(2)
    }

    @Test
    fun `updateExerciseSet updates set and maintains relationships`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val originalSet = ExerciseSet.createDefault("Original", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(originalSet)

        val updatedSet = db.setDao().getExerciseSet(setId)!!.copy(
            exerciseName = "Updated Exercise",
            lowerReps = 10,
            higherReps = 15,
            restTime = 120
        )

        db.setDao().updateExerciseSet(updatedSet)

        val retrievedSet = db.setDao().getExerciseSet(setId)
        assertThat(retrievedSet!!.exerciseName).isEqualTo("Updated Exercise")
        assertThat(retrievedSet.lowerReps).isEqualTo(10)
        assertThat(retrievedSet.higherReps).isEqualTo(15)
        assertThat(retrievedSet.restTime).isEqualTo(120)
        assertThat(retrievedSet.cycleId).isEqualTo(cycleId) // Relationships maintained
        assertThat(retrievedSet.workoutId).isEqualTo(workoutId)
    }

    @Test
    fun `updateSetSync updates set without return value`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val originalSet = ExerciseSet.createDefault("Original", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(originalSet)

        val updatedSet = db.setDao().getExerciseSet(setId)!!.copy(
            exerciseName = "Sync Updated",
            position = 3
        )

        db.setDao().updateSetSync(updatedSet)

        val retrievedSet = db.setDao().getExerciseSet(setId)
        assertThat(retrievedSet!!.exerciseName).isEqualTo("Sync Updated")
        assertThat(retrievedSet.position).isEqualTo(3)
    }

    @Test
    fun `updateMinReps updates only lowerReps field`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        db.setDao().updateMinReps(setId, 6)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.lowerReps).isEqualTo(6)
        assertThat(updatedSet.higherReps).isEqualTo(12) // Should remain unchanged
        assertThat(updatedSet.restTime).isEqualTo(60) // Should remain unchanged
        assertThat(updatedSet.exerciseName).isEqualTo("Test Exercise") // Should remain unchanged
    }

    @Test
    fun `updateMaxReps updates only higherReps field`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        db.setDao().updateMaxReps(setId, 15)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.higherReps).isEqualTo(15)
        assertThat(updatedSet.lowerReps).isEqualTo(8) // Should remain unchanged
        assertThat(updatedSet.restTime).isEqualTo(60) // Should remain unchanged
    }

    @Test
    fun `updateRestTime updates only restTime field`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        db.setDao().updateRestTime(setId, 120)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.restTime).isEqualTo(120)
        assertThat(updatedSet.lowerReps).isEqualTo(8) // Should remain unchanged
        assertThat(updatedSet.higherReps).isEqualTo(12) // Should remain unchanged
    }

    @Test
    fun `updatePosition updates only position field`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        db.setDao().updatePosition(setId, 5)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.position).isEqualTo(5)
        assertThat(updatedSet.exerciseName).isEqualTo("Test Exercise") // Should remain unchanged
        assertThat(updatedSet.lowerReps).isEqualTo(8) // Should remain unchanged
    }

    @Test
    fun `updateExerciseName updates only exerciseName field`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Original Name", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        db.setDao().updateExerciseName(setId, "New Exercise Name")

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.exerciseName).isEqualTo("New Exercise Name")
        assertThat(updatedSet.lowerReps).isEqualTo(8) // Should remain unchanged
        assertThat(updatedSet.higherReps).isEqualTo(12) // Should remain unchanged
        assertThat(updatedSet.restTime).isEqualTo(60) // Should remain unchanged
    }

    @Test
    fun `deleteExerciseSet removes set`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("To Delete", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)
        val setToDelete = db.setDao().getExerciseSet(setId)!!

        db.setDao().deleteExerciseSet(setToDelete)

        val retrievedSet = db.setDao().getExerciseSet(setId)
        assertThat(retrievedSet).isNull()

        val allSets = db.setDao().getAllExerciseSets()
        assertThat(allSets).isEmpty()
    }

    @Test
    fun `insertSet with replace strategy replaces existing set`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val set1 = ExerciseSet(1L, 1, cycleId, workoutId, "Original", 8, 12, 60)
        val set2 = ExerciseSet(1L, 2, cycleId, workoutId, "Replaced", 6, 10, 90)

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2) // Should replace due to OnConflictStrategy.REPLACE

        val allSets = db.setDao().getAllExerciseSets()
        assertThat(allSets).hasSize(1)
        assertThat(allSets[0].exerciseName).isEqualTo("Replaced")
        assertThat(allSets[0].position).isEqualTo(2)
        assertThat(allSets[0].lowerReps).isEqualTo(6)
        assertThat(allSets[0].higherReps).isEqualTo(10)
        assertThat(allSets[0].restTime).isEqualTo(90)
    }

    @Test
    fun `foreign key cascade delete removes sets when workout is deleted`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val workout = db.workoutDao().getWorkoutById(workoutId)!!

        val set1 = ExerciseSet.createDefault("Exercise 1", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val set2 = ExerciseSet.createDefault("Exercise 2", 6, 10, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 2
        }

        db.setDao().insertSet(set1)
        db.setDao().insertSet(set2)

        // Verify sets exist
        val setsBeforeDelete = db.setDao().getExerciseSetsSync(cycleId, workoutId)
        assertThat(setsBeforeDelete).hasSize(2)

        // Delete the workout
        db.workoutDao().deleteWorkout(workout)

        // Verify sets are cascade deleted
        val setsAfterDelete = db.setDao().getAllExerciseSets()
        assertThat(setsAfterDelete).isEmpty()
    }

    @Test
    fun `multiple workouts with sets work independently`() = runTest {
        val (cycleId1, workoutId1, _) = createTestHierarchy()
        val (cycleId2, workoutId2, _) = createTestHierarchy()

        val workout1Set1 = ExerciseSet.createDefault("W1 Exercise 1", 8, 12, 60).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 1
        }
        val workout1Set2 = ExerciseSet.createDefault("W1 Exercise 2", 6, 10, 90).apply {
            this.cycleId = cycleId1
            this.workoutId = workoutId1
            this.position = 2
        }
        val workout2Set1 = ExerciseSet.createDefault("W2 Exercise 1", 10, 15, 45).apply {
            this.cycleId = cycleId2
            this.workoutId = workoutId2
            this.position = 1
        }

        db.setDao().insertSet(workout1Set1)
        db.setDao().insertSet(workout1Set2)
        db.setDao().insertSet(workout2Set1)

        val workout1Sets = db.setDao().getExerciseSetsSync(cycleId1, workoutId1)
        val workout2Sets = db.setDao().getExerciseSetsSync(cycleId2, workoutId2)
        val allSets = db.setDao().getAllExerciseSets()

        assertThat(workout1Sets).hasSize(2)
        assertThat(workout2Sets).hasSize(1)
        assertThat(allSets).hasSize(3)

        assertThat(workout1Sets[0].exerciseName).isEqualTo("W1 Exercise 1")
        assertThat(workout1Sets[1].exerciseName).isEqualTo("W1 Exercise 2")
        assertThat(workout2Sets[0].exerciseName).isEqualTo("W2 Exercise 1")
    }

    @Test
    fun `exercise set properties are preserved correctly`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet(
            id = 0L,
            position = 3,
            cycleId = cycleId,
            workoutId = workoutId,
            exerciseName = "Complex Exercise",
            lowerReps = 5,
            higherReps = 8,
            restTime = 180
        )

        val setId = db.setDao().insertSetSync(exerciseSet)
        val retrievedSet = db.setDao().getExerciseSet(setId)

        assertThat(retrievedSet).isNotNull()
        assertThat(retrievedSet!!.position).isEqualTo(3)
        assertThat(retrievedSet.cycleId).isEqualTo(cycleId)
        assertThat(retrievedSet.workoutId).isEqualTo(workoutId)
        assertThat(retrievedSet.exerciseName).isEqualTo("Complex Exercise")
        assertThat(retrievedSet.lowerReps).isEqualTo(5)
        assertThat(retrievedSet.higherReps).isEqualTo(8)
        assertThat(retrievedSet.restTime).isEqualTo(180)
    }

    @Test
    fun `multiple field updates work correctly together`() = runTest {
        val (cycleId, workoutId, _) = createTestHierarchy()
        val exerciseSet = ExerciseSet.createDefault("Multi Update Test", 8, 12, 60).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        // Update multiple fields
        db.setDao().updateMinReps(setId, 6)
        db.setDao().updateMaxReps(setId, 15)
        db.setDao().updateRestTime(setId, 120)
        db.setDao().updateExerciseName(setId, "Updated Exercise")
        db.setDao().updatePosition(setId, 3)

        val updatedSet = db.setDao().getExerciseSet(setId)
        assertThat(updatedSet!!.lowerReps).isEqualTo(6)
        assertThat(updatedSet.higherReps).isEqualTo(15)
        assertThat(updatedSet.restTime).isEqualTo(120)
        assertThat(updatedSet.exerciseName).isEqualTo("Updated Exercise")
        assertThat(updatedSet.position).isEqualTo(3)
    }
}