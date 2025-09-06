package com.musclehack.targetedHypertrophyTraining.utilities

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "tht.db"
private const val SCHEMA_VERSION = 13

class LegacyDatabaseHelper private constructor(ctxt: Context) : SQLiteOpenHelper(
    ctxt, DATABASE_NAME,
    null, SCHEMA_VERSION
) {
    companion object {
        private var singleton: LegacyDatabaseHelper? = null

        @Synchronized
        fun getInstance(ctxt: Context): LegacyDatabaseHelper {
            if (singleton == null)
                singleton = LegacyDatabaseHelper(ctxt.applicationContext)

            return singleton as LegacyDatabaseHelper
        }
    }

    class CYCLES {
        companion object {
            const val TABLE = "Cycles"
            const val ID = "CycleID"
            const val NAME = "CycleName"
            const val DURATION = "CycleDuration"
            const val POSITION = "Position"
            const val DATE_CREATED = "DateCreated"
            const val LOG_DATE = "DateLastLogged"
        }
    }

    class WORKOUTS {
        companion object {
            const val TABLE = "Workouts"
            const val ID = "WorkoutID"
            const val NAME = "WorkoutName"
            const val POSITION = "Position"
            const val CYCLE_ID = "CycleID"
            const val REPEATS = "Repeats"
            const val LAST_DAY_VIEWED = "LastDayViewed"
            const val LAST_SET_VIEWED = "LastSetViewed"
        }
    }

    class SETS {
        companion object {
            const val TABLE = "Sets"
            const val ID = "SetID"
            const val POSITION = "Position"
            const val CYCLE_ID = "CycleID"
            const val WORKOUT_ID = "WorkoutID"
            const val EXERCISE_NAME = "ExerciseName"
            const val LOWER_REPS = "LowerReps"
            const val HIGHER_REPS = "HigherReps"
            const val REST_TIME = "RestTime"
        }
    }

    class LOGS {
        companion object {
            const val TABLE = "ExerciseLogs"
            const val SET_ID = "SetID"
            const val DAY = "Day"
            const val DATE_LOGGED = "DateLogged"
            const val WEIGHT = "Weight"
            const val REPS = "Reps"
            const val NOTE = "Note"
            const val SUB_NAME = "SubName"
            const val SUB_WEIGHT = "SubWeight"
            const val SUB_REPS = "SubReps"
            const val SKIP = "Skip"
        }
    }

    private class CUSTOM_EX {
        companion object {
            const val TABLE = "CustomExercises"
            const val ID = "CustomExerciseID"
            const val NAME = "CustomExerciseName"
        }
    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
}