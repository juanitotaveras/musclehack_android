package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.musclehack.targetedHypertrophyTraining.MainCoroutineRule
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
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
class UserExerciseDaoTest {

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
    fun `insertExercise inserts user exercise correctly`() = runTest {
        val userExercise = UserExercise(0L, "Custom Push-ups")

        db.userExerciseDao().insertExercise(userExercise)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(1)

        val retrievedExercise = allExercises[0]
        assertThat(retrievedExercise.name).isEqualTo("Custom Push-ups")
        assertThat(retrievedExercise.id).isGreaterThan(0) // Auto-generated ID
    }

    @Test
    fun `getAllUserExercises returns all exercises`() = runTest {
        val exercise1 = UserExercise(0L, "Custom Squats")
        val exercise2 = UserExercise(0L, "Custom Deadlifts")
        val exercise3 = UserExercise(0L, "Custom Bench Press")

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao().insertExercise(exercise2)
        db.userExerciseDao().insertExercise(exercise3)

        val allExercises = db.userExerciseDao().getAllUserExercises()

        assertThat(allExercises).hasSize(3)
        val exerciseNames = allExercises.map { it.name }
        assertThat(exerciseNames).containsExactly(
            "Custom Squats",
            "Custom Deadlifts",
            "Custom Bench Press"
        )
    }

    @Test
    fun `getUserExercise returns specific exercise when exists`() = runTest {
        val userExercise = UserExercise(0L, "Custom Pull-ups")

        db.userExerciseDao().insertExercise(userExercise)

        val retrievedExercise = db.userExerciseDao().getUserExercise("Custom Pull-ups")

        assertThat(retrievedExercise).isNotNull()
        assertThat(retrievedExercise!!.name).isEqualTo("Custom Pull-ups")
        assertThat(retrievedExercise.id).isGreaterThan(0)
    }

    @Test
    fun `getUserExercise returns null when exercise does not exist`() = runTest {
        val retrievedExercise = db.userExerciseDao().getUserExercise("Non-existent Exercise")

        assertThat(retrievedExercise).isNull()
    }

    @Test
    fun `deleteUserExercise removes exercise by name`() = runTest {
        val exercise1 = UserExercise(0L, "To Keep")
        val exercise2 = UserExercise(0L, "To Delete")

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao().insertExercise(exercise2)

        // Verify both exercises exist
        val exercisesBeforeDelete = db.userExerciseDao().getAllUserExercises()
        assertThat(exercisesBeforeDelete).hasSize(2)

        // Delete one exercise
        db.userExerciseDao().deleteUserExercise("To Delete")

        // Verify only one exercise remains
        val exercisesAfterDelete = db.userExerciseDao().getAllUserExercises()
        assertThat(exercisesAfterDelete).hasSize(1)
        assertThat(exercisesAfterDelete[0].name).isEqualTo("To Keep")

        // Verify the deleted exercise is gone
        val deletedExercise = db.userExerciseDao().getUserExercise("To Delete")
        assertThat(deletedExercise).isNull()
    }

    @Test
    fun `deleteUserExercise with non-existent name does nothing`() = runTest {
        val userExercise = UserExercise(0L, "Existing Exercise")

        db.userExerciseDao().insertExercise(userExercise)

        // Try to delete non-existent exercise
        db.userExerciseDao().deleteUserExercise("Non-existent Exercise")

        // Verify original exercise is still there
        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(1)
        assertThat(allExercises[0].name).isEqualTo("Existing Exercise")
    }

    @Test
    fun `insertExercise with replace strategy replaces existing exercise`() = runTest {
        val exercise1 = UserExercise(1L, "Original Exercise")
        val exercise2 = UserExercise(1L, "Replaced Exercise")

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao()
            .insertExercise(exercise2) // Should replace due to OnConflictStrategy.REPLACE

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(1)
        assertThat(allExercises[0].name).isEqualTo("Replaced Exercise")
        assertThat(allExercises[0].id).isEqualTo(1L)
    }

    @Test
    fun `auto-generated IDs work correctly`() = runTest {
        val exercise1 = UserExercise(0L, "Exercise 1") // ID will be auto-generated
        val exercise2 = UserExercise(0L, "Exercise 2") // ID will be auto-generated

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao().insertExercise(exercise2)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(2)

        val ids = allExercises.map { it.id }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids.all { it > 0 }).isTrue()
    }

    @Test
    fun `exercise names are case sensitive`() = runTest {
        val exercise1 = UserExercise(0L, "Push-ups")
        val exercise2 = UserExercise(0L, "push-ups")

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao().insertExercise(exercise2)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(2)

        val retrievedExercise1 = db.userExerciseDao().getUserExercise("Push-ups")
        val retrievedExercise2 = db.userExerciseDao().getUserExercise("push-ups")
        val retrievedExercise3 = db.userExerciseDao().getUserExercise("PUSH-UPS")

        assertThat(retrievedExercise1).isNotNull()
        assertThat(retrievedExercise2).isNotNull()
        assertThat(retrievedExercise3).isNull() // Case sensitive
    }

    @Test
    fun `exercise names with special characters work correctly`() = runTest {
        val exercisesWithSpecialChars = listOf(
            UserExercise(0L, "Pull-ups (Wide Grip)"),
            UserExercise(0L, "Dumbbell Curls 21's"),
            UserExercise(0L, "Smith Machine Squats"),
            UserExercise(0L, "Cable Flyes - Upper"),
            UserExercise(0L, "Leg Press @ 45°")
        )

        exercisesWithSpecialChars.forEach { exercise ->
            db.userExerciseDao().insertExercise(exercise)
        }

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(5)

        // Test retrieval of exercises with special characters
        val retrievedExercise1 = db.userExerciseDao().getUserExercise("Pull-ups (Wide Grip)")
        val retrievedExercise2 = db.userExerciseDao().getUserExercise("Dumbbell Curls 21's")
        val retrievedExercise3 = db.userExerciseDao().getUserExercise("Leg Press @ 45°")

        assertThat(retrievedExercise1).isNotNull()
        assertThat(retrievedExercise2).isNotNull()
        assertThat(retrievedExercise3).isNotNull()
    }

    @Test
    fun `empty exercise name is handled correctly`() = runTest {
        val exerciseWithEmptyName = UserExercise(0L, "")

        db.userExerciseDao().insertExercise(exerciseWithEmptyName)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(1)
        assertThat(allExercises[0].name).isEmpty()

        val retrievedExercise = db.userExerciseDao().getUserExercise("")
        assertThat(retrievedExercise).isNotNull()
        assertThat(retrievedExercise!!.name).isEmpty()
    }

    @Test
    fun `very long exercise names are handled correctly`() = runTest {
        val longName = "A".repeat(1000) // Very long exercise name
        val exerciseWithLongName = UserExercise(0L, longName)

        db.userExerciseDao().insertExercise(exerciseWithLongName)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(1)
        assertThat(allExercises[0].name).hasLength(1000)

        val retrievedExercise = db.userExerciseDao().getUserExercise(longName)
        assertThat(retrievedExercise).isNotNull()
        assertThat(retrievedExercise!!.name).isEqualTo(longName)
    }

    @Test
    fun `duplicate exercise names with different IDs are handled correctly`() = runTest {
        val exercise1 = UserExercise(0L, "Duplicate Name") // Auto-generated ID
        val exercise2 = UserExercise(0L, "Duplicate Name") // Auto-generated ID

        db.userExerciseDao().insertExercise(exercise1)
        db.userExerciseDao().insertExercise(exercise2)

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(2)

        // Both exercises should have the same name but different IDs
        assertThat(allExercises.all { it.name == "Duplicate Name" }).isTrue()
        assertThat(allExercises.map { it.id }).containsNoDuplicates()

        // getUserExercise should return one of them (database behavior may vary)
        val retrievedExercise = db.userExerciseDao().getUserExercise("Duplicate Name")
        assertThat(retrievedExercise).isNotNull()
        assertThat(retrievedExercise!!.name).isEqualTo("Duplicate Name")
    }

    @Test
    fun `large number of exercises are handled correctly`() = runTest {
        val exerciseCount = 100
        val exercises = (1..exerciseCount).map { index ->
            UserExercise(0L, "Exercise $index")
        }

        // Insert all exercises
        exercises.forEach { exercise ->
            db.userExerciseDao().insertExercise(exercise)
        }

        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(exerciseCount)

        // Test retrieval of specific exercises
        val retrievedExercise1 = db.userExerciseDao().getUserExercise("Exercise 1")
        val retrievedExercise50 = db.userExerciseDao().getUserExercise("Exercise 50")
        val retrievedExercise100 = db.userExerciseDao().getUserExercise("Exercise 100")

        assertThat(retrievedExercise1).isNotNull()
        assertThat(retrievedExercise50).isNotNull()
        assertThat(retrievedExercise100).isNotNull()
    }

    @Test
    fun `multiple delete operations work correctly`() = runTest {
        val exercises = listOf(
            UserExercise(0L, "Exercise A"),
            UserExercise(0L, "Exercise B"),
            UserExercise(0L, "Exercise C"),
            UserExercise(0L, "Exercise D"),
            UserExercise(0L, "Exercise E")
        )

        // Insert all exercises
        exercises.forEach { exercise ->
            db.userExerciseDao().insertExercise(exercise)
        }

        // Verify all exercises are inserted
        assertThat(db.userExerciseDao().getAllUserExercises()).hasSize(5)

        // Delete exercises one by one
        db.userExerciseDao().deleteUserExercise("Exercise B")
        assertThat(db.userExerciseDao().getAllUserExercises()).hasSize(4)

        db.userExerciseDao().deleteUserExercise("Exercise D")
        assertThat(db.userExerciseDao().getAllUserExercises()).hasSize(3)

        db.userExerciseDao().deleteUserExercise("Exercise A")
        assertThat(db.userExerciseDao().getAllUserExercises()).hasSize(2)

        // Verify remaining exercises
        val remainingExercises = db.userExerciseDao().getAllUserExercises()
        val remainingNames = remainingExercises.map { it.name }
        assertThat(remainingNames).containsExactly("Exercise C", "Exercise E")
    }

    @Test
    fun `toString method returns exercise name`() = runTest {
        val userExercise = UserExercise(1L, "Test Exercise")

        assertThat(userExercise.toString()).isEqualTo("Test Exercise")
    }

    @Test
    fun `areContentsEqual method works correctly`() = runTest {
        val exercise1 = UserExercise(1L, "Same Exercise")
        val exercise2 = UserExercise(1L, "Same Exercise")
        val exercise3 = UserExercise(2L, "Same Exercise")
        val exercise4 = UserExercise(1L, "Different Exercise")

        assertThat(exercise1.areContentsEqual(exercise2)).isTrue() // Same ID and name
        assertThat(exercise1.areContentsEqual(exercise3)).isFalse() // Different ID
        assertThat(exercise1.areContentsEqual(exercise4)).isFalse() // Different name
    }

    @Test
    fun `exercise bank workflow simulation`() = runTest {
        // Simulate a typical exercise bank workflow

        // 1. Add some custom exercises
        val customExercises = listOf(
            UserExercise(0L, "Custom Burpees"),
            UserExercise(0L, "Custom Mountain Climbers"),
            UserExercise(0L, "Custom Plank Variations")
        )

        customExercises.forEach { exercise ->
            db.userExerciseDao().insertExercise(exercise)
        }

        // 2. Verify exercises were added
        val allExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(allExercises).hasSize(3)

        // 3. Search for a specific exercise
        val foundExercise = db.userExerciseDao().getUserExercise("Custom Burpees")
        assertThat(foundExercise).isNotNull()

        // 4. Add a duplicate exercise (should replace due to strategy)
        val duplicateExercise = UserExercise(foundExercise!!.id, "Updated Burpees")
        db.userExerciseDao().insertExercise(duplicateExercise)

        // 5. Verify the exercise was updated
        val updatedExercise = db.userExerciseDao().getUserExercise("Updated Burpees")
        assertThat(updatedExercise).isNotNull()
        assertThat(updatedExercise!!.id).isEqualTo(foundExercise.id)

        // 6. Remove an exercise
        db.userExerciseDao().deleteUserExercise("Custom Mountain Climbers")

        // 7. Verify final state
        val finalExercises = db.userExerciseDao().getAllUserExercises()
        assertThat(finalExercises).hasSize(2)
        val finalNames = finalExercises.map { it.name }
        assertThat(finalNames).containsExactly("Updated Burpees", "Custom Plank Variations")
    }
}
