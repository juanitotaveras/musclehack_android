package com.musclehack.targetedHypertrophyTraining.data.repository

import android.content.Context
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule
import com.musclehack.targetedHypertrophyTraining.data.source.TrackerDataSource
import com.musclehack.targetedHypertrophyTraining.exerciseBank.Exercises
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DefaultCycleCreationRepository @Inject constructor(
    @AppModule.TrackerLocalDataSource private val trackerLocalDataSource: TrackerDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val context: Context
) : CycleCreationRepository {
    override suspend fun createThreeDayFull(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    ) {
        val exStrings = arrayOf(
            arrayOf(
                Exercises.BB_SQUATS,
                Exercises.BB_SQUATS,
                Exercises.DECLINE_BB_BENCH_PRESS,
                Exercises.DECLINE_BB_BENCH_PRESS,
                Exercises.SEATED_CALF_RAISES,
                Exercises.SEATED_CALF_RAISES,
                Exercises.REVERSE_CRUNCHES,
                Exercises.REVERSE_CRUNCHES,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_INCLINE_DB_CURLS,
                Exercises.SEATED_INCLINE_DB_CURLS,
                Exercises.SEATED_CABLE_ROWS,
                Exercises.SEATED_CABLE_ROWS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.CABLE_SHRUGS,
                Exercises.CABLE_SHRUGS,
                Exercises.DB_WRIST_CURLS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.DEEP_CHEST_DIPS,
                Exercises.DEEP_CHEST_DIPS,
                Exercises.ONE_ARM_DB_ROWS,
                Exercises.ONE_ARM_DB_ROWS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.CONCENTRATION_CURLS,
                Exercises.CONCENTRATION_CURLS,
                Exercises.CABLE_BENT_OVER_TRICEP_EXTENSIONS,
                Exercises.CABLE_BENT_OVER_TRICEP_EXTENSIONS,
                Exercises.LEG_PRESS,
                Exercises.LEG_PRESS,
                Exercises.LEG_EXTENSIONS,
                Exercises.LEG_CURLS
            )
                .map { getStr(it) }.toTypedArray()
        )
        val dayNames = arrayOf("monday_and_friday", "wednesday")
            .map { getStr(it) }.toTypedArray()
        val cycle = Cycle.createDefault(name = newCycleName, numWeeks = newCycleDuration)
        val workoutList = listOf(
            hashMapOf(
                // Monday and Friday
                Pair(
                    Workout.createDefault(dayNames[0], 2, 0), listOf(
                        ExerciseSet.createDefault(exStrings[0][0], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][2], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][3], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][7], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][9], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][10], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][11], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][12], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][13], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][14], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][15], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][16], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][17], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][18], lowerReps, higherReps, 0)
                    )
                ),
                // Wednesday
                Pair(
                    Workout.createDefault(dayNames[1], 1, 1), listOf(
                        ExerciseSet.createDefault(exStrings[1][0], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[1][1], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[1][2], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[1][3], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][4], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][5], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][6], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][7], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][8], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][10], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][11], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][12], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][13], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][14], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][15], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][16], lowerReps, higherReps, 0)
                    )
                )
            )
        )
        val nestedCycle = hashMapOf(Pair(cycle, workoutList))
        trackerLocalDataSource.createNestedCycle(nestedCycle)
    }

    override suspend fun createBlank(newCycleName: String, numWeeks: Int) {
        trackerLocalDataSource.createBlankCycle(newCycleName, numWeeks)
    }

    override suspend fun createThreeDaySplit(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    ) {
        val exStrings = arrayOf(
            arrayOf(
                Exercises.BB_SQUATS,
                Exercises.BB_SQUATS,
                Exercises.BB_SQUATS,
                Exercises.LEG_PRESS,
                Exercises.LEG_PRESS,
                Exercises.LEG_PRESS,
                Exercises.LEG_CURLS,
                Exercises.LEG_EXTENSIONS,
                Exercises.SEATED_CALF_RAISES,
                Exercises.SEATED_CALF_RAISES,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.LAT_RAISE_MACHINE,
                Exercises.LAT_RAISE_MACHINE,
                Exercises.BB_FRONT_RAISES
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.FLAT_DB_BENCH_PRESS,
                Exercises.FLAT_DB_BENCH_PRESS,
                Exercises.DECLINE_BB_BENCH_PRESS,
                Exercises.DECLINE_BB_BENCH_PRESS,
                Exercises.PEC_DECK, Exercises.PEC_DECK,
                Exercises.INCLINE_BENCH_PRESS,
                Exercises.LEG_RAISES_CAPTAIN,
                Exercises.LEG_RAISES_CAPTAIN,
                Exercises.REVERSE_CRUNCHES,
                Exercises.REVERSE_CRUNCHES,
                Exercises.MACHINE_CRUNCHES,
                Exercises.MACHINE_CRUNCHES,
                Exercises.STANDING_BB_CURLS,
                Exercises.STANDING_BB_CURLS,
                Exercises.STANDING_DB_CURLS,
                Exercises.STANDING_DB_CURLS,
                Exercises.MACHINE_PREACHER_CURLS,
                Exercises.MACHINE_PREACHER_CURLS,
                Exercises.CONCENTRATION_CURLS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.ONE_ARM_DB_ROWS,
                Exercises.ONE_ARM_DB_ROWS,
                Exercises.PULLUPS, Exercises.PULLUPS,
                Exercises.WEIGHTED_TRICEP_DIPS,
                Exercises.WEIGHTED_TRICEP_DIPS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.DECLINE_TRICEP_EXTENSIONS,
                Exercises.DECLINE_TRICEP_EXTENSIONS,
                Exercises.ONE_ARM_REVERSE_PUSHDOWNS,
                Exercises.ONE_ARM_REVERSE_PUSHDOWNS,
                Exercises.DB_SHRUGS,
                Exercises.DB_SHRUGS,
                Exercises.DB_WRIST_CURLS
            )
                .map { getStr(it) }.toTypedArray()
        )
        val dayNames = arrayOf("monday", "wednesday", "friday")
            .map { getStr(it) }.toTypedArray()
        val cycle = Cycle.createDefault(name = newCycleName, numWeeks = newCycleDuration)
        val workoutList = listOf(
            hashMapOf(
                Pair(
                    Workout.createDefault(dayNames[0], 1, 0), listOf(
                        ExerciseSet.createDefault(exStrings[0][0], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][2], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][3], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][4], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][6], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][7], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][9], lowerReps, higherReps, 300),
                        ExerciseSet.createDefault(exStrings[0][10], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][11], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][12], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][13], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][14], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][15], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][16], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][17], lowerReps, higherReps, 0)
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[1], 1, 1), listOf(
                        ExerciseSet.createDefault(exStrings[1][0], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][2], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][3], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][7], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][10], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][11], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][12], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][13], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][14], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][15], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][16], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][17], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][18], lowerReps, higherReps, 180)
                        // TODO: this rest time should be 0
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[2], 1, 2), listOf(
                        ExerciseSet.createDefault(exStrings[2][0], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[2][1], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[2][2], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[2][3], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][4], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][5], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][6], lowerReps, higherReps, 300),
                        ExerciseSet.createDefault(exStrings[2][7], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][8], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][10], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][11], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][12], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][13], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][14], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][15], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][16], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][17], lowerReps, higherReps, 0)
                    )
                )
            )
        )
        val nestedCycle = hashMapOf(Pair(cycle, workoutList))
        trackerLocalDataSource.createNestedCycle(nestedCycle)
    }

    override suspend fun createFiveDaySplit(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    ) {
        val dayNames = arrayOf("monday", "tuesday", "wednesday", "thursday", "friday")
            .map { getStr(it) }.toTypedArray()
        val exStrings = arrayOf(
            arrayOf(
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_BB_PRESS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.SEATED_OH_DB_PRESS,
                Exercises.LAT_RAISE_MACHINE,
                Exercises.LAT_RAISE_MACHINE,
                Exercises.DB_FRONT_RAISES,
                Exercises.SMITH_MACHINE_SHRUGS,
                Exercises.SMITH_MACHINE_SHRUGS,
                Exercises.SMITH_MACHINE_SHRUGS,
                Exercises.SMITH_MACHINE_SHRUGS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.BB_SQUATS,
                Exercises.BB_SQUATS,
                Exercises.LEG_PRESS,
                Exercises.LEG_PRESS,
                Exercises.STIFF_LEGGED_DEADLIFTS,
                Exercises.STIFF_LEGGED_DEADLIFTS,
                Exercises.LEG_EXTENSIONS,
                Exercises.LEG_CURLS,
                Exercises.CALF_RAISE_ON_LEG_PRESS,
                Exercises.CALF_RAISE_ON_LEG_PRESS,
                Exercises.CALF_RAISE_ON_LEG_PRESS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.CABLE_TRICEP_PUSHDOWNS,
                Exercises.WEIGHTED_TRICEP_DIPS,
                Exercises.WEIGHTED_TRICEP_DIPS,
                Exercises.WEIGHTED_TRICEP_DIPS,
                Exercises.DECLINE_CABLE_TRICEP_EXTENSIONS,
                Exercises.DECLINE_CABLE_TRICEP_EXTENSIONS,
                Exercises.MACHINE_PREACHER_CURLS,
                Exercises.MACHINE_PREACHER_CURLS,
                Exercises.CONCENTRATION_CURLS,
                Exercises.CONCENTRATION_CURLS,
                Exercises.CONCENTRATION_CURLS,
                Exercises.STANDING_BB_CURLS,
                Exercises.STANDING_BB_CURLS,
                Exercises.REVERSE_WRIST_CURLS,
                Exercises.REVERSE_WRIST_CURLS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.DEADLIFT,
                Exercises.PULLUPS,
                Exercises.PULLUPS,
                Exercises.PULLUPS,
                Exercises.ONE_ARM_DB_ROWS,
                Exercises.ONE_ARM_DB_ROWS
            )
                .map { getStr(it) }.toTypedArray(),
            arrayOf(
                Exercises.FLAT_DB_BENCH_PRESS,
                Exercises.FLAT_DB_BENCH_PRESS,
                Exercises.DECLINE_DB_BENCH_PRESS,
                Exercises.DECLINE_DB_BENCH_PRESS,
                Exercises.INCLINE_BENCH_PRESS,
                Exercises.INCLINE_BENCH_PRESS,
                Exercises.PEC_DECK,
                Exercises.PEC_DECK,
                Exercises.HANGING_LEG_RAISES,
                Exercises.HANGING_LEG_RAISES,
                Exercises.DECLINE_SIT_UPS,
                Exercises.DECLINE_SIT_UPS,
                Exercises.MACHINE_CRUNCHES,
                Exercises.MACHINE_CRUNCHES
            )
                .map { getStr(it) }.toTypedArray()
        )
        val cycle = Cycle.createDefault(name = newCycleName, numWeeks = newCycleDuration)
        val workoutList = listOf(
            hashMapOf(
                Pair(
                    Workout.createDefault(dayNames[0], 1, 0), listOf(
                        ExerciseSet.createDefault(exStrings[0][0], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][2], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[0][3], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][5], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][7], lowerReps, higherReps, 240),
                        ExerciseSet.createDefault(exStrings[0][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][10], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[0][11], lowerReps, higherReps, 0)
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[1], 1, 1), listOf(
                        ExerciseSet.createDefault(exStrings[1][0], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][2], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][3], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][4], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][5], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][7], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[1][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[1][10], lowerReps, higherReps, 0)
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[2], 1, 2), listOf(
                        ExerciseSet.createDefault(exStrings[2][0], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][1], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][2], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][3], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][7], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][9], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][10], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][11], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][12], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][13], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][14], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[2][15], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[2][16], lowerReps, higherReps, 0)
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[3], 1, 3), listOf(
                        ExerciseSet.createDefault(exStrings[3][0], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[3][1], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[3][2], 4, 6, 180),
                        ExerciseSet.createDefault(exStrings[3][3], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[3][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[3][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[3][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[3][7], lowerReps, higherReps, 0)
                    )
                ),
                Pair(
                    Workout.createDefault(dayNames[4], 1, 4), listOf(
                        ExerciseSet.createDefault(exStrings[4][0], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][1], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[4][2], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][3], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[4][4], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][5], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[4][6], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][7], lowerReps, higherReps, 180),
                        ExerciseSet.createDefault(exStrings[4][8], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][9], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][10], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][11], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][12], lowerReps, higherReps, 120),
                        ExerciseSet.createDefault(exStrings[4][13], lowerReps, higherReps, 0)
                    )
                )
            )
        )
        val nestedCycle = hashMapOf(Pair(cycle, workoutList))
        trackerLocalDataSource.createNestedCycle(nestedCycle)
    }

    private fun getStr(name: String): String {
        val resId = context.resources.getIdentifier(
            name,
            "string", context.packageName
        )
        if (resId > 0)
            return context.getString(resId) as String
        return ""
    }
}