package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.musclehack.targetedHypertrophyTraining.MainCoroutineRule
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseLog
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseNote
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
class ExerciseNoteDaoTest {

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
        // Create cycle -> workout -> exerciseSet -> exerciseLog hierarchy
        val cycleId = db.cycleDao().insertCycle(Cycle.createDefault("Test Cycle", 8))
        val workout = Workout.createDefault("Test Workout", 3, 1).apply { this.cycleId = cycleId }
        val workoutId = db.workoutDao().insertWorkout(workout)
        val exerciseSet = ExerciseSet.createDefault("Test Exercise", 8, 12, 90).apply {
            this.cycleId = cycleId
            this.workoutId = workoutId
            this.position = 1
        }
        val setId = db.setDao().insertSetSync(exerciseSet)

        // Create the required ExerciseLog for foreign key constraint
        val exerciseLog = ExerciseLog.createDefault(setId, 1).apply { hasNote = true }
        db.exerciseLogDao().insertLog(exerciseLog)

        return Triple(cycleId, workoutId, setId)
    }

    @Test
    fun `insertExerciseNote inserts note correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val currentTime = System.currentTimeMillis()
        val exerciseNote = ExerciseNote(setId, 1, "Great workout today!", currentTime)

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.setId).isEqualTo(setId)
        assertThat(retrievedNote.day).isEqualTo(1)
        assertThat(retrievedNote.note).isEqualTo("Great workout today!")
        assertThat(retrievedNote.date).isEqualTo(currentTime)
    }

    @Test
    fun `insertExerciseNote with replace strategy replaces existing note`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val note1 = ExerciseNote(setId, 1, "Original note", 1000L)
        val note2 = ExerciseNote(setId, 1, "Updated note", 2000L)

        db.exerciseNoteDao().insertExerciseNote(note1)
        db.exerciseNoteDao()
            .insertExerciseNote(note2) // Should replace due to OnConflictStrategy.REPLACE

        val allNotes = db.exerciseNoteDao().getAllExerciseNotes()
        assertThat(allNotes).hasSize(1)
        assertThat(allNotes[0].note).isEqualTo("Updated note")
        assertThat(allNotes[0].date).isEqualTo(2000L)
    }

    @Test
    fun `getExerciseNote returns specific note when exists`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseNote = ExerciseNote(setId, 1, "Find this note", System.currentTimeMillis())

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)

        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.setId).isEqualTo(setId)
        assertThat(retrievedNote.day).isEqualTo(1)
        assertThat(retrievedNote.note).isEqualTo("Find this note")
    }

    @Test
    fun `getExerciseNote returns null when note does not exist`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 999)

        assertThat(retrievedNote).isNull()
    }

    @Test
    fun `getExerciseNotes returns all notes for specific set`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        // Create multiple exercise logs for different days
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId1, 2).apply { hasNote = true })
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId1, 3).apply { hasNote = true })
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId2, 1).apply { hasNote = true })

        val note1 = ExerciseNote(setId1, 1, "Day 1 note", 1000L)
        val note2 = ExerciseNote(setId1, 2, "Day 2 note", 2000L)
        val note3 = ExerciseNote(setId1, 3, "Day 3 note", 3000L)
        val note4 = ExerciseNote(setId2, 1, "Different set note", 4000L)

        db.exerciseNoteDao().insertExerciseNote(note1)
        db.exerciseNoteDao().insertExerciseNote(note2)
        db.exerciseNoteDao().insertExerciseNote(note3)
        db.exerciseNoteDao().insertExerciseNote(note4)

        val set1Notes = db.exerciseNoteDao().getExerciseNotes(setId1)

        assertThat(set1Notes).hasSize(3)
        val noteTexts = set1Notes.map { it.note }
        assertThat(noteTexts).containsExactly("Day 1 note", "Day 2 note", "Day 3 note")
    }

    @Test
    fun `getAllExerciseNotes returns all notes in database`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        // Create exercise logs for foreign key constraint
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId2, 1).apply { hasNote = true })

        val note1 = ExerciseNote(setId1, 1, "Note 1", 1000L)
        val note2 = ExerciseNote(setId2, 1, "Note 2", 2000L)

        db.exerciseNoteDao().insertExerciseNote(note1)
        db.exerciseNoteDao().insertExerciseNote(note2)

        val allNotes = db.exerciseNoteDao().getAllExerciseNotes()

        assertThat(allNotes).hasSize(2)
        val noteTexts = allNotes.map { it.note }
        assertThat(noteTexts).containsExactly("Note 1", "Note 2")
    }

    @Test
    fun `updateExerciseNote updates existing note`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val originalNote = ExerciseNote(setId, 1, "Original note", 1000L)

        db.exerciseNoteDao().insertExerciseNote(originalNote)

        val updatedNote = originalNote.copy(
            note = "Updated note content",
            date = 2000L
        )

        db.exerciseNoteDao().updateExerciseNote(updatedNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(retrievedNote!!.note).isEqualTo("Updated note content")
        assertThat(retrievedNote.date).isEqualTo(2000L)
        assertThat(retrievedNote.setId).isEqualTo(setId) // Primary key maintained
        assertThat(retrievedNote.day).isEqualTo(1) // Primary key maintained
    }

    @Test
    fun `composite primary key prevents duplicate setId and day combinations`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val note1 = ExerciseNote(setId, 1, "First note", 1000L)
        val note2 = ExerciseNote(setId, 1, "Second note", 2000L) // Same setId and day

        db.exerciseNoteDao().insertExerciseNote(note1)
        db.exerciseNoteDao()
            .insertExerciseNote(note2) // Should replace due to composite primary key

        val allNotes = db.exerciseNoteDao().getAllExerciseNotes()
        assertThat(allNotes).hasSize(1)
        assertThat(allNotes[0].note).isEqualTo("Second note") // Should be the second note
        assertThat(allNotes[0].date).isEqualTo(2000L)
    }

    @Test
    fun `foreign key cascade delete removes notes when exercise log is deleted`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseNote =
            ExerciseNote(setId, 1, "This note will be deleted", System.currentTimeMillis())

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        // Verify note exists
        val noteBeforeDelete = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(noteBeforeDelete).isNotNull()

        // Delete the exercise log (parent entity)
        val exerciseLog = db.exerciseLogDao().getExerciseLog(setId, 1)!!
        // Since ExerciseLog doesn't have a delete method, we'll delete the ExerciseSet instead
        // which will cascade delete the ExerciseLog and then the ExerciseNote
        val exerciseSet = db.setDao().getExerciseSet(setId)!!
        db.setDao().deleteExerciseSet(exerciseSet)

        // Verify note is cascade deleted
        val noteAfterDelete = db.exerciseNoteDao().getAllExerciseNotes()
        assertThat(noteAfterDelete).isEmpty()
    }

    @Test
    fun `multiple sets with notes work independently`() = runTest {
        val (_, _, setId1) = createTestHierarchy()
        val (_, _, setId2) = createTestHierarchy()

        // Create exercise logs for foreign key constraint
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId1, 2).apply { hasNote = true })
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId2, 1).apply { hasNote = true })
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId2, 2).apply { hasNote = true })

        val set1Note1 = ExerciseNote(setId1, 1, "Set1 Day1 note", 1000L)
        val set1Note2 = ExerciseNote(setId1, 2, "Set1 Day2 note", 2000L)
        val set2Note1 = ExerciseNote(setId2, 1, "Set2 Day1 note", 3000L)
        val set2Note2 = ExerciseNote(setId2, 2, "Set2 Day2 note", 4000L)

        db.exerciseNoteDao().insertExerciseNote(set1Note1)
        db.exerciseNoteDao().insertExerciseNote(set1Note2)
        db.exerciseNoteDao().insertExerciseNote(set2Note1)
        db.exerciseNoteDao().insertExerciseNote(set2Note2)

        val set1Notes = db.exerciseNoteDao().getExerciseNotes(setId1)
        val set2Notes = db.exerciseNoteDao().getExerciseNotes(setId2)
        val allNotes = db.exerciseNoteDao().getAllExerciseNotes()

        assertThat(set1Notes).hasSize(2)
        assertThat(set2Notes).hasSize(2)
        assertThat(allNotes).hasSize(4)

        assertThat(set1Notes.map { it.note }).containsExactly("Set1 Day1 note", "Set1 Day2 note")
        assertThat(set2Notes.map { it.note }).containsExactly("Set2 Day1 note", "Set2 Day2 note")
    }

    @Test
    fun `exercise note properties are preserved correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val noteDate = System.currentTimeMillis()
        val exerciseNote = ExerciseNote(
            setId = setId,
            day = 1,
            note = "This is a detailed note about my workout performance and how I felt during the exercise.",
            date = noteDate
        )

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)
        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)

        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.setId).isEqualTo(setId)
        assertThat(retrievedNote.day).isEqualTo(1)
        assertThat(retrievedNote.note).isEqualTo("This is a detailed note about my workout performance and how I felt during the exercise.")
        assertThat(retrievedNote.date).isEqualTo(noteDate)
    }

    @Test
    fun `empty note content is handled correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val exerciseNote = ExerciseNote(setId, 1, "", System.currentTimeMillis())

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.note).isEmpty()
    }

    @Test
    fun `very long note content is handled correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val longNote = "A".repeat(5000) // Very long note
        val exerciseNote = ExerciseNote(setId, 1, longNote, System.currentTimeMillis())

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.note).hasLength(5000)
        assertThat(retrievedNote.note).isEqualTo(longNote)
    }

    @Test
    fun `note content with special characters is handled correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val specialNote =
            "Note with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~\nNew line\tTab character"
        val exerciseNote = ExerciseNote(setId, 1, specialNote, System.currentTimeMillis())

        db.exerciseNoteDao().insertExerciseNote(exerciseNote)

        val retrievedNote = db.exerciseNoteDao().getExerciseNote(setId, 1)
        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote!!.note).isEqualTo(specialNote)
    }

    @Test
    fun `notes across different days for same set work correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        // Create exercise logs for multiple days
        for (day in 2..5) {
            db.exerciseLogDao()
                .insertLog(ExerciseLog.createDefault(setId, day).apply { hasNote = true })
        }

        val notes = listOf(
            ExerciseNote(setId, 1, "Day 1: First workout", 1000L),
            ExerciseNote(setId, 2, "Day 2: Feeling stronger", 2000L),
            ExerciseNote(setId, 3, "Day 3: Increased weight", 3000L),
            ExerciseNote(setId, 4, "Day 4: Perfect form today", 4000L),
            ExerciseNote(setId, 5, "Day 5: Personal record!", 5000L)
        )

        notes.forEach { note ->
            db.exerciseNoteDao().insertExerciseNote(note)
        }

        val retrievedNotes = db.exerciseNoteDao().getExerciseNotes(setId)
        assertThat(retrievedNotes).hasSize(5)

        // Verify each day's note
        for (day in 1..5) {
            val dayNote = db.exerciseNoteDao().getExerciseNote(setId, day)
            assertThat(dayNote).isNotNull()
            assertThat(dayNote!!.note).contains("Day $day")
        }
    }

    @Test
    fun `note timestamps are preserved and can be used for ordering`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        // Create exercise logs for different days
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId, 2).apply { hasNote = true })
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId, 3).apply { hasNote = true })

        val note1 = ExerciseNote(setId, 1, "Oldest note", 1000L)
        val note2 = ExerciseNote(setId, 2, "Middle note", 2000L)
        val note3 = ExerciseNote(setId, 3, "Newest note", 3000L)

        db.exerciseNoteDao().insertExerciseNote(note2) // Insert out of order
        db.exerciseNoteDao().insertExerciseNote(note3)
        db.exerciseNoteDao().insertExerciseNote(note1)

        val allNotes = db.exerciseNoteDao().getExerciseNotes(setId)
        val sortedByDate = allNotes.sortedBy { it.date }

        assertThat(sortedByDate[0].note).isEqualTo("Oldest note")
        assertThat(sortedByDate[1].note).isEqualTo("Middle note")
        assertThat(sortedByDate[2].note).isEqualTo("Newest note")
    }

    @Test
    fun `workout note workflow simulation`() = runTest {
        val (_, _, setId) = createTestHierarchy()

        // Simulate a workout note workflow over multiple days

        // Day 1: Add initial note
        val day1Note = ExerciseNote(setId, 1, "First day back at gym", System.currentTimeMillis())
        db.exerciseNoteDao().insertExerciseNote(day1Note)

        // Day 2: Add another day's note
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId, 2).apply { hasNote = true })
        val day2Note = ExerciseNote(
            setId,
            2,
            "Increased weight by 5lbs",
            System.currentTimeMillis() + 86400000
        )
        db.exerciseNoteDao().insertExerciseNote(day2Note)

        // Day 3: Add note then update it
        db.exerciseLogDao().insertLog(ExerciseLog.createDefault(setId, 3).apply { hasNote = true })
        val day3Note =
            ExerciseNote(setId, 3, "Initial note", System.currentTimeMillis() + 172800000)
        db.exerciseNoteDao().insertExerciseNote(day3Note)

        val updatedDay3Note = day3Note.copy(
            note = "Updated: Hit personal record today!",
            date = System.currentTimeMillis() + 172800001
        )
        db.exerciseNoteDao().updateExerciseNote(updatedDay3Note)

        // Verify final state
        val finalNotes = db.exerciseNoteDao().getExerciseNotes(setId)
        assertThat(finalNotes).hasSize(3)

        val day1Retrieved = db.exerciseNoteDao().getExerciseNote(setId, 1)
        val day2Retrieved = db.exerciseNoteDao().getExerciseNote(setId, 2)
        val day3Retrieved = db.exerciseNoteDao().getExerciseNote(setId, 3)

        assertThat(day1Retrieved!!.note).isEqualTo("First day back at gym")
        assertThat(day2Retrieved!!.note).isEqualTo("Increased weight by 5lbs")
        assertThat(day3Retrieved!!.note).isEqualTo("Updated: Hit personal record today!")
    }

    @Test
    fun `large number of notes are handled correctly`() = runTest {
        val (_, _, setId) = createTestHierarchy()
        val noteCount = 50

        // Create exercise logs for all days
        for (day in 2..noteCount) {
            db.exerciseLogDao()
                .insertLog(ExerciseLog.createDefault(setId, day).apply { hasNote = true })
        }

        // Insert many notes
        for (day in 1..noteCount) {
            val note =
                ExerciseNote(setId, day, "Note for day $day", System.currentTimeMillis() + day)
            db.exerciseNoteDao().insertExerciseNote(note)
        }

        val allNotes = db.exerciseNoteDao().getExerciseNotes(setId)
        assertThat(allNotes).hasSize(noteCount)

        // Test retrieval of specific notes
        val note1 = db.exerciseNoteDao().getExerciseNote(setId, 1)
        val note25 = db.exerciseNoteDao().getExerciseNote(setId, 25)
        val note50 = db.exerciseNoteDao().getExerciseNote(setId, noteCount)

        assertThat(note1!!.note).isEqualTo("Note for day 1")
        assertThat(note25!!.note).isEqualTo("Note for day 25")
        assertThat(note50!!.note).isEqualTo("Note for day $noteCount")
    }
}
