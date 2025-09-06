package com.musclehack.targetedHypertrophyTraining.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.*


@Database(
    entities = [
        Cycle::class,
        Workout::class,
        ExerciseSet::class,
        ExerciseLog::class,
        ExerciseNote::class,
        UserExercise::class],

    version = 15, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun setDao(): ExerciseSetDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cycleDao(): CycleDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun exerciseNoteDao(): ExerciseNoteDao
    abstract fun userExerciseDao(): UserExerciseDao
}