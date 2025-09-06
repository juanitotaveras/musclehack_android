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
class WorkoutDaoTest {

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

    @Test
    fun `insertWorkout inserts workout correctly`() = runTest {
        // First create a cycle since workout has foreign key relationship
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Push Day", 3, 1).apply {
            this.cycleId = cycleId
        }

        val workoutId = db.workoutDao().insertWorkout(workout)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()
        assertThat(retrievedWorkout!!.name).isEqualTo("Push Day")
        assertThat(retrievedWorkout.repeats).isEqualTo(3)
        assertThat(retrievedWorkout.position).isEqualTo(1)
        assertThat(retrievedWorkout.cycleId).isEqualTo(cycleId)
    }

    @Test
    fun `insertWorkoutSync inserts workout and returns ID`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Pull Day", 2, 2).apply {
            this.cycleId = cycleId
        }

        val workoutId = db.workoutDao().insertWorkoutSync(workout)

        assertThat(workoutId).isGreaterThan(0)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()
        assertThat(retrievedWorkout!!.name).isEqualTo("Pull Day")
        assertThat(retrievedWorkout.repeats).isEqualTo(2)
    }

    @Test
    fun `getAllWorkouts returns all workouts ordered by position`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))

        val workout1 = Workout.createDefault("Workout 1", 3, 3).apply { this.cycleId = cycleId }
        val workout2 = Workout.createDefault("Workout 2", 2, 1).apply { this.cycleId = cycleId }
        val workout3 = Workout.createDefault("Workout 3", 4, 2).apply { this.cycleId = cycleId }

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2)
        db.workoutDao().insertWorkout(workout3)

        val workouts = db.workoutDao().getAllWorkouts()

        assertThat(workouts).hasSize(3)
        assertThat(workouts[0].name).isEqualTo("Workout 2") // position 1
        assertThat(workouts[1].name).isEqualTo("Workout 3") // position 2
        assertThat(workouts[2].name).isEqualTo("Workout 1") // position 3
    }

    @Test
    fun `observeAllWorkouts returns LiveData with all workouts ordered by position`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))

        val workout1 = Workout.createDefault("Workout A", 3, 2).apply { this.cycleId = cycleId }
        val workout2 = Workout.createDefault("Workout B", 2, 1).apply { this.cycleId = cycleId }

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2)

        val workoutsLiveData = db.workoutDao().observeAllWorkouts()
        workoutsLiveData.getOrAwaitValue()

        val workouts = workoutsLiveData.value
        assertThat(workouts).isNotNull()
        assertThat(workouts).hasSize(2)
        assertThat(workouts!![0].name).isEqualTo("Workout B") // position 1
        assertThat(workouts[1].name).isEqualTo("Workout A") // position 2
    }

    @Test
    fun `getWorkouts returns LiveData with workouts for specific cycle`() = runTest {
        val cycleId1 = db.cycleDao().insertCycle(Cycle.createDefault("Cycle 1", 8))
        val cycleId2 = db.cycleDao().insertCycle(Cycle.createDefault("Cycle 2", 10))

        val workout1 =
            Workout.createDefault("Cycle1 Workout1", 3, 2).apply { this.cycleId = cycleId1 }
        val workout2 =
            Workout.createDefault("Cycle1 Workout2", 2, 1).apply { this.cycleId = cycleId1 }
        val workout3 =
            Workout.createDefault("Cycle2 Workout1", 4, 1).apply { this.cycleId = cycleId2 }

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2)
        db.workoutDao().insertWorkout(workout3)

        val cycle1WorkoutsLiveData = db.workoutDao().getWorkouts(cycleId1)
        cycle1WorkoutsLiveData.getOrAwaitValue()

        val cycle1Workouts = cycle1WorkoutsLiveData.value
        assertThat(cycle1Workouts).isNotNull()
        assertThat(cycle1Workouts).hasSize(2)
        assertThat(cycle1Workouts!![0].name).isEqualTo("Cycle1 Workout2") // position 1
        assertThat(cycle1Workouts[1].name).isEqualTo("Cycle1 Workout1") // position 2
    }

    @Test
    fun `getWorkoutsSync returns workouts for specific cycle synchronously`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))

        val workout1 = Workout.createDefault("Workout 1", 3, 2).apply { this.cycleId = cycleId }
        val workout2 = Workout.createDefault("Workout 2", 2, 1).apply { this.cycleId = cycleId }

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2)

        val workouts = db.workoutDao().getWorkoutsSync(cycleId)

        assertThat(workouts).hasSize(2)
        assertThat(workouts[0].name).isEqualTo("Workout 2") // position 1
        assertThat(workouts[1].name).isEqualTo("Workout 1") // position 2
    }

    @Test
    fun `getWorkoutById returns specific workout when exists`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Find Me", 5, 1).apply { this.cycleId = cycleId }

        val workoutId = db.workoutDao().insertWorkout(workout)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)

        assertThat(retrievedWorkout).isNotNull()
        assertThat(retrievedWorkout!!.id).isEqualTo(workoutId)
        assertThat(retrievedWorkout.name).isEqualTo("Find Me")
        assertThat(retrievedWorkout.repeats).isEqualTo(5)
    }

    @Test
    fun `getWorkoutById returns null when workout does not exist`() = runTest {
        val retrievedWorkout = db.workoutDao().getWorkoutById(999L)

        assertThat(retrievedWorkout).isNull()
    }

    @Test
    fun `updateWorkout updates workout and returns count`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val originalWorkout =
            Workout.createDefault("Original", 3, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(originalWorkout)

        val updatedWorkout = db.workoutDao().getWorkoutById(workoutId)!!.copy(
            name = "Updated Workout",
            position = 2
        )

        val updateCount = db.workoutDao().updateWorkout(updatedWorkout)

        assertThat(updateCount).isEqualTo(1)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(retrievedWorkout!!.name).isEqualTo("Updated Workout")
        assertThat(retrievedWorkout.position).isEqualTo(2)
    }

    @Test
    fun `updateWorkoutSync updates workout without return value`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val originalWorkout =
            Workout.createDefault("Original", 3, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(originalWorkout)

        val updatedWorkout = db.workoutDao().getWorkoutById(workoutId)!!.copy(
            name = "Updated Sync",
            lastDayViewed = 5
        )

        db.workoutDao().updateWorkoutSync(updatedWorkout)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(retrievedWorkout!!.name).isEqualTo("Updated Sync")
        assertThat(retrievedWorkout.lastDayViewed).isEqualTo(5)
    }

    @Test
    fun `updateWorkoutName updates only the name field`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Original Name", 4, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(workout)

        db.workoutDao().updateWorkoutName(workoutId, "New Name")

        val updatedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(updatedWorkout!!.name).isEqualTo("New Name")
        assertThat(updatedWorkout.repeats).isEqualTo(4) // Should remain unchanged
        assertThat(updatedWorkout.position).isEqualTo(1) // Should remain unchanged
    }

    @Test
    fun `deleteWorkout removes workout`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("To Delete", 2, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(workout)
        val workoutToDelete = db.workoutDao().getWorkoutById(workoutId)!!

        db.workoutDao().deleteWorkout(workoutToDelete)

        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNull()

        val allWorkouts = db.workoutDao().getAllWorkouts()
        assertThat(allWorkouts).isEmpty()
    }

    @Test
    fun `insertWorkout with replace strategy replaces existing workout`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout1 = Workout("Original", 1L, 1, 3, -1, -1, cycleId)
        val workout2 = Workout("Replaced", 1L, 2, 5, -1, -1, cycleId)

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2) // Should replace due to OnConflictStrategy.REPLACE

        val workouts = db.workoutDao().getAllWorkouts()
        assertThat(workouts).hasSize(1)
        assertThat(workouts[0].name).isEqualTo("Replaced")
        assertThat(workouts[0].position).isEqualTo(2)
        assertThat(workouts[0].repeats).isEqualTo(5)
    }

    @Test
    fun `foreign key cascade delete removes workouts when cycle is deleted`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val cycle = db.cycleDao().getCycleById(cycleId)!!

        val workout1 = Workout.createDefault("Workout 1", 3, 1).apply { this.cycleId = cycleId }
        val workout2 = Workout.createDefault("Workout 2", 2, 2).apply { this.cycleId = cycleId }

        db.workoutDao().insertWorkout(workout1)
        db.workoutDao().insertWorkout(workout2)

        // Verify workouts exist
        val workoutsBeforeDelete = db.workoutDao().getWorkoutsSync(cycleId)
        assertThat(workoutsBeforeDelete).hasSize(2)

        // Delete the cycle
        db.cycleDao().deleteCycle(cycle)

        // Verify workouts are cascade deleted
        val workoutsAfterDelete = db.workoutDao().getAllWorkouts()
        assertThat(workoutsAfterDelete).isEmpty()
    }

    @Test
    fun `multiple cycles with workouts work independently`() = runTest {
        val cycle1Id = db.cycleDao().insertCycle(Cycle.createDefault("Cycle 1", 8))
        val cycle2Id = db.cycleDao().insertCycle(Cycle.createDefault("Cycle 2", 10))

        val cycle1Workout1 =
            Workout.createDefault("C1 Workout 1", 3, 1).apply { this.cycleId = cycle1Id }
        val cycle1Workout2 =
            Workout.createDefault("C1 Workout 2", 2, 2).apply { this.cycleId = cycle1Id }
        val cycle2Workout1 =
            Workout.createDefault("C2 Workout 1", 4, 1).apply { this.cycleId = cycle2Id }

        db.workoutDao().insertWorkout(cycle1Workout1)
        db.workoutDao().insertWorkout(cycle1Workout2)
        db.workoutDao().insertWorkout(cycle2Workout1)

        val cycle1Workouts = db.workoutDao().getWorkoutsSync(cycle1Id)
        val cycle2Workouts = db.workoutDao().getWorkoutsSync(cycle2Id)
        val allWorkouts = db.workoutDao().getAllWorkouts()

        assertThat(cycle1Workouts).hasSize(2)
        assertThat(cycle2Workouts).hasSize(1)
        assertThat(allWorkouts).hasSize(3)

        assertThat(cycle1Workouts[0].name).isEqualTo("C1 Workout 1")
        assertThat(cycle1Workouts[1].name).isEqualTo("C1 Workout 2")
        assertThat(cycle2Workouts[0].name).isEqualTo("C2 Workout 1")
    }

    @Test
    fun `workout properties are preserved correctly`() = runTest {
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout(
            name = "Complex Workout",
            id = 0L,
            position = 3,
            repeats = 5,
            lastDayViewed = 2,
            lastSetViewed = 4,
            cycleId = cycleId
        )

        val workoutId = db.workoutDao().insertWorkout(workout)
        val retrievedWorkout = db.workoutDao().getWorkoutById(workoutId)

        assertThat(retrievedWorkout).isNotNull()
        assertThat(retrievedWorkout!!.name).isEqualTo("Complex Workout")
        assertThat(retrievedWorkout.position).isEqualTo(3)
        assertThat(retrievedWorkout.repeats).isEqualTo(5)
        assertThat(retrievedWorkout.lastDayViewed).isEqualTo(2)
        assertThat(retrievedWorkout.lastSetViewed).isEqualTo(4)
        assertThat(retrievedWorkout.cycleId).isEqualTo(cycleId)
    }
}
