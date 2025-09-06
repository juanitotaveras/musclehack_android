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
class CycleDaoTest {

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
    fun `insertCycle inserts cycle correctly`() = runTest {
        db.cycleDao().insertCycle(Cycle.createDefault("one", 10))

        val c: Cycle = db.cycleDao().getCycles()[0]

        assertThat(c).isNotNull()
        assertThat(c.numWeeks).isEqualTo(10)
        assertThat(c.name).isEqualTo("one")
    }

    @Test
    fun `getCycles returns cycles ordered by position`() = runTest {
        // Insert cycles with different positions
        val cycle1 = Cycle.createDefault("Cycle 1", 12).apply { position = 2 }
        val cycle2 = Cycle.createDefault("Cycle 2", 8).apply { position = 1 }
        val cycle3 = Cycle.createDefault("Cycle 3", 6).apply { position = 3 }

        db.cycleDao().insertCycle(cycle1)
        db.cycleDao().insertCycle(cycle2)
        db.cycleDao().insertCycle(cycle3)

        val cycles = db.cycleDao().getCycles()

        assertThat(cycles).hasSize(3)
        assertThat(cycles[0].name).isEqualTo("Cycle 2") // position 1
        assertThat(cycles[1].name).isEqualTo("Cycle 1") // position 2
        assertThat(cycles[2].name).isEqualTo("Cycle 3") // position 3
    }

    @Test
    fun `observeCycles returns LiveData with cycles ordered by position`() = runTest {
        // Insert cycles with different positions
        val cycle1 = Cycle.createDefault("Cycle A", 10).apply { position = 3 }
        val cycle2 = Cycle.createDefault("Cycle B", 8).apply { position = 1 }

        db.cycleDao().insertCycle(cycle1)
        db.cycleDao().insertCycle(cycle2)

        val cyclesLiveData = db.cycleDao().observeCycles()
        cyclesLiveData.getOrAwaitValue()

        val cycles = cyclesLiveData.value

        assertThat(cycles).isNotNull()
        assertThat(cycles).hasSize(2)
        assertThat(cycles!![0].name).isEqualTo("Cycle B") // position 1
        assertThat(cycles[1].name).isEqualTo("Cycle A") // position 3
    }

    @Test
    fun `insertCycleSync inserts cycle and returns ID`() = runTest {
        val cycle = Cycle.createDefault("Sync Cycle", 16)

        val insertedId = db.cycleDao().insertCycleSync(cycle)

        assertThat(insertedId).isGreaterThan(0)

        val retrievedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(retrievedCycle).isNotNull()
        assertThat(retrievedCycle!!.name).isEqualTo("Sync Cycle")
        assertThat(retrievedCycle.numWeeks).isEqualTo(16)
    }

    @Test
    fun `updateCycle updates cycle and returns count`() = runTest {
        val originalCycle = Cycle.createDefault("Original", 8)
        val insertedId = db.cycleDao().insertCycle(originalCycle)

        val updatedCycle = db.cycleDao().getCycleById(insertedId)!!.copy(
            name = "Updated Cycle",
            numWeeks = 12
        )

        val updateCount = db.cycleDao().updateCycle(updatedCycle)

        assertThat(updateCount).isEqualTo(1)

        val retrievedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(retrievedCycle!!.name).isEqualTo("Updated Cycle")
        assertThat(retrievedCycle.numWeeks).isEqualTo(12)
    }

    @Test
    fun `updateCycleName updates only the name field`() = runTest {
        val cycle = Cycle.createDefault("Original Name", 10)
        val insertedId = db.cycleDao().insertCycle(cycle)

        db.cycleDao().updateCycleName(insertedId, "New Name")

        val updatedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(updatedCycle!!.name).isEqualTo("New Name")
        assertThat(updatedCycle.numWeeks).isEqualTo(10) // Should remain unchanged
    }

    @Test
    fun `deleteCycle removes cycle and returns count`() = runTest {
        val cycle = Cycle.createDefault("To Delete", 6)
        val insertedId = db.cycleDao().insertCycle(cycle)
        val cycleToDelete = db.cycleDao().getCycleById(insertedId)!!

        val deleteCount = db.cycleDao().deleteCycle(cycleToDelete)

        assertThat(deleteCount).isEqualTo(1)

        val retrievedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(retrievedCycle).isNull()

        val allCycles = db.cycleDao().getCycles()
        assertThat(allCycles).isEmpty()
    }

    @Test
    fun `getCycleById returns specific cycle when exists`() = runTest {
        val cycle = Cycle.createDefault("Find Me", 18)
        val insertedId = db.cycleDao().insertCycle(cycle)

        val retrievedCycle = db.cycleDao().getCycleById(insertedId)

        assertThat(retrievedCycle).isNotNull()
        assertThat(retrievedCycle!!.id).isEqualTo(insertedId)
        assertThat(retrievedCycle.name).isEqualTo("Find Me")
        assertThat(retrievedCycle.numWeeks).isEqualTo(18)
    }

    @Test
    fun `getCycleById returns null when cycle does not exist`() = runTest {
        val retrievedCycle = db.cycleDao().getCycleById(999L)

        assertThat(retrievedCycle).isNull()
    }

    @Test
    fun `updateTrainingDate updates dateLastLogged field`() = runTest {
        val cycle = Cycle.createDefault("Training Cycle", 8)
        val insertedId = db.cycleDao().insertCycle(cycle)
        val newDate = System.currentTimeMillis()

        db.cycleDao().updateTrainingDate(insertedId, newDate)

        val updatedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(updatedCycle!!.dateLastLogged).isEqualTo(newDate)
    }

    @Test
    fun `updatePositionSync updates position field`() = runTest {
        val cycle = Cycle.createDefault("Position Test", 10)
        val insertedId = db.cycleDao().insertCycle(cycle)
        val newPosition = 5

        db.cycleDao().updatePositionSync(insertedId, newPosition)

        val updatedCycle = db.cycleDao().getCycleById(insertedId)
        assertThat(updatedCycle!!.position).isEqualTo(newPosition)
    }

    @Test
    fun `insertCycle with replace strategy replaces existing cycle`() = runTest {
        val cycle1 = Cycle(1L, "Original", 0, System.currentTimeMillis(), 0, 8)
        val cycle2 = Cycle(1L, "Replaced", 0, System.currentTimeMillis(), 0, 12)

        db.cycleDao().insertCycle(cycle1)
        db.cycleDao().insertCycle(cycle2) // Should replace due to OnConflictStrategy.REPLACE

        val cycles = db.cycleDao().getCycles()
        assertThat(cycles).hasSize(1)
        assertThat(cycles[0].name).isEqualTo("Replaced")
        assertThat(cycles[0].numWeeks).isEqualTo(12)
    }

    @Test
    fun `multiple operations work together correctly`() = runTest {
        // Insert multiple cycles
        val cycle1 = Cycle.createDefault("Cycle 1", 8).apply { position = 1 }
        val cycle2 = Cycle.createDefault("Cycle 2", 10).apply { position = 2 }
        val id1 = db.cycleDao().insertCycle(cycle1)
        val id2 = db.cycleDao().insertCycle(cycle2)

        // Update one cycle's name
        db.cycleDao().updateCycleName(id1, "Updated Cycle 1")

        // Update training date for the second cycle
        val newDate = System.currentTimeMillis()
        db.cycleDao().updateTrainingDate(id2, newDate)

        // Verify all changes
        val cycles = db.cycleDao().getCycles()
        assertThat(cycles).hasSize(2)

        val retrievedCycle1 = cycles.find { it.id == id1 }
        val retrievedCycle2 = cycles.find { it.id == id2 }

        assertThat(retrievedCycle1!!.name).isEqualTo("Updated Cycle 1")
        assertThat(retrievedCycle2!!.dateLastLogged).isEqualTo(newDate)
    }
}