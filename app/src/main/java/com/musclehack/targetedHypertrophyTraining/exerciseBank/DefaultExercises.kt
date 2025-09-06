/*
Author: Juanito Taveras
Created: 11/2/17
Modified: 11/2/17
 */

package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.res.Resources
import com.musclehack.targetedHypertrophyTraining.R
import java.util.*

class DefaultExercises/* might need an exercise list object */
    (private val res: Resources, private val pkg: String) {
    var exercises: HashMap<String, List<ExerciseBankItem>> = HashMap()
    val categories: ArrayList<String> = ArrayList()
    private val allExerciseNames: ArrayList<String> = ArrayList()
    val allExercises: ArrayList<ExerciseBankItem> = ArrayList()


    init {
        // make categories

        //"legs_title",
        val exercises = arrayOf(
            arrayOf(
                arrayOf(
                    Exercises.BB_SQUATS,
                    "http://www.exrx.net/WeightExercises/Quadriceps/BBSquat.html"
                ),
                arrayOf(
                    Exercises.LEG_PRESS,
                    "http://www.exrx.net/WeightExercises/Quadriceps/LV45LegPress.html"
                ),
                arrayOf(
                    Exercises.BB_LUNGES,
                    "http://www.exrx.net/WeightExercises/Quadriceps/BBLunge.html"
                ),
                arrayOf(
                    Exercises.DB_LUNGES,
                    "http://www.exrx.net/WeightExercises/Quadriceps/DBLunge.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.STIFF_LEGGED_DEADLIFTS,
                    "http://www.exrx.net/WeightExercises/Hamstrings/BBStraightLegDeadlift.html"
                ),
                arrayOf(
                    Exercises.LEG_CURLS,
                    "http://www.exrx.net/WeightExercises/Hamstrings/LVSeatedLegCurl.html"
                )
            ), arrayOf(
                arrayOf(
                    Exercises.LEG_EXTENSIONS,
                    "http://www.exrx.net/WeightExercises/Quadriceps/LVLegExtension.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.STANDING_CALF_RAISES,
                    "http://www.exrx.net/WeightExercises/Gastrocnemius/BBStandingCalfRaise.html"
                ),
                arrayOf(
                    Exercises.SEATED_CALF_RAISES,
                    "https://www.musclehack.com/build-muscular-calves-with-calf-raises/"
                ),
                arrayOf(
                    Exercises.HACK_SQUAT_MACHINE_CALF_RAISES,
                    "http://www.exrx.net/WeightExercises/Gastrocnemius/LVStandingCalfRaise.html"
                ),
                arrayOf(
                    Exercises.CALF_RAISE_ON_LEG_PRESS,
                    "http://www.exrx.net/WeightExercises/Gastrocnemius/LVSeatedCalfPressH.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.CHIN_UPS,
                    "http://www.exrx.net/WeightExercises/LatissimusDorsi/BWUnderhandChinup.html"
                ),
                arrayOf(
                    Exercises.CLOSE_GRIP_PULLDOWNS,
                    "https://www.musclehack.com/how-to-do-close-grip-pulldowns/"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.MACHINE_PREACHER_CURLS,
                    "https://www.musclehack.com/build-huge-biceps-with-this-unique-exercise/"
                ),
                arrayOf(
                    Exercises.STANDING_BB_CURLS,
                    "https://www.musclehack.com/barbell-bicep-curls/"
                ),
                arrayOf(
                    Exercises.STANDING_DB_CURLS,
                    "http://www.exrx.net/WeightExercises/Biceps/DBCurl.html"
                ),
                arrayOf(Exercises.EZ_BAR_CURLS, "https://www.musclehack.com/barbell-bicep-curls/"),
                arrayOf(
                    Exercises.SEATED_INCLINE_DB_CURLS,
                    "https://www.musclehack.com/incline-dumbbell-curls/"
                ),
                arrayOf(
                    Exercises.CONCENTRATION_CURLS,
                    "https://www.musclehack.com/how-to-do-concentration-curls-correctly/"
                ),
                arrayOf(
                    Exercises.ROPE_CURLS,
                    "https://www.exrx.net/WeightExercises/Brachioradialis/CBHammerCurl"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.DECLINE_TRICEP_EXTENSIONS,
                    "https://www.musclehack.com/how-to-perform-french-curls-skull-crushers-correctly/"
                ),
                arrayOf(
                    Exercises.DECLINE_CABLE_TRICEP_EXTENSIONS,
                    "http://www.exrx.net/WeightExercises/Triceps/CBDeclineTricepsExt.html"
                ),
                arrayOf(
                    Exercises.WEIGHTED_TRICEP_DIPS,
                    "http://www.exrx.net/WeightExercises/Triceps/BWTriDip.html"
                ),
                arrayOf(
                    Exercises.CLOSE_GRIP_BENCH_PRESS,
                    "http://www.exrx.net/WeightExercises/Triceps/BBCloseGripBenchPress.html"
                ),
                arrayOf(
                    Exercises.CABLE_BENT_OVER_TRICEP_EXTENSIONS,
                    "https://www.musclehack.com/cable-bent-over-triceps-extensions/"
                ),
                arrayOf(
                    Exercises.CABLE_TRICEP_PUSHDOWNS,
                    "https://www.musclehack.com/the-mcmanus-tricep-pushdown/"
                ),
                arrayOf(Exercises.PINWHEEL_CURLS, "https://www.musclehack.com/pinwheel-curls/"),
                arrayOf(
                    Exercises.ONE_ARM_REVERSE_PUSHDOWNS,
                    "https://www.musclehack.com/reverse-pushdown/"
                ),
                arrayOf(
                    Exercises.LOW_CABLE_KICKBACKS,
                    "https://www.musclehack.com/how-to-do-tricep-cable-kickbacks/"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.FLAT_BB_BENCH_PRESS,
                    "http://www.exrx.net/WeightExercises/PectoralSternal/BBBenchPress.html"
                ),
                arrayOf(
                    Exercises.FLAT_DB_BENCH_PRESS,
                    "http://www.exrx.net/WeightExercises/PectoralSternal/DBBenchPress.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.DECLINE_BB_BENCH_PRESS,
                    "http://www.exrx.net/WeightExercises/PectoralSternal/BBDeclineBenchPress.html"
                ),
                arrayOf(
                    Exercises.DECLINE_DB_BENCH_PRESS,
                    "https://www.musclehack.com/how-to-do-decline-dumbbell-bench-press-for-a-big-chest/"
                ),
                arrayOf(
                    Exercises.DEEP_CHEST_DIPS,
                    "https://www.musclehack.com/how-to-do-chest-dips/"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.INCLINE_BENCH_PRESS,
                    "http://www.exrx.net/WeightExercises/PectoralClavicular/CBInclineBenchPress.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.PEC_DECK,
                    "https://www.musclehack.com/how-to-build-muscular-pecs-like-a-cartoon-superhero/"
                ),
                arrayOf(
                    Exercises.CABLE_CROSSOVERS,
                    "http://www.exrx.net/WeightExercises/PectoralSternal/CBStandingFly.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.DEADLIFT,
                    "http://www.exrx.net/WeightExercises/ErectorSpinae/BBDeadlift.html"
                ),
                arrayOf(
                    Exercises.RACK_PULLS,
                    "https://exrx.net/WeightExercises/ErectorSpinae/BBRackPull"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.LAT_PULLDOWNS,
                    "https://www.musclehack.com/how-to-do-lat-pull-downs-correctly/"
                ),
                arrayOf(
                    Exercises.PULLUPS,
                    "http://www.exrx.net/WeightExercises/LatissimusDorsi/BWPullup.html"
                ),
                arrayOf(
                    Exercises.BENT_OVER_BB_ROWS,
                    "https://www.musclehack.com/how-to-do-bent-over-barbell-rows-video/"
                ),
                arrayOf(Exercises.SEATED_CABLE_ROWS, "https://www.musclehack.com/cable-rows/"),
                arrayOf(Exercises.ONE_ARM_DB_ROWS, "https://www.musclehack.com/dumbbell-rows/"),
                arrayOf(
                    Exercises.PULLOVER_MACHINE,
                    "http://www.exrx.net/WeightExercises/LatissimusDorsi/LVPullover.html"
                ),
                arrayOf(
                    Exercises.KNEELING_PULLOVERS,
                    "https://www.musclehack.com/kneeling-cable-bent-over-pullovers/"
                ),
                arrayOf(
                    Exercises.DECLINE_CABLE_PULLOVERS,
                    "https://www.musclehack.com/how-to-get-lats-like-a-bats/"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.HYPEREXTENSIONS,
                    "http://www.exrx.net/WeightExercises/ErectorSpinae/BBHyperextension.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.STANDING_OH_BB_PRESS,
                    "https://www.musclehack.com/military-press-overhead-press-video-tutorial/"
                ),
                arrayOf(
                    Exercises.STANDING_OH_DB_PRESS,
                    "https://www.musclehack.com/overhead-dumbbell-press/"
                ),
                arrayOf(
                    Exercises.SEATED_OH_BB_PRESS,
                    "https://www.musclehack.com/military-press-overhead-press-video-tutorial/"
                ),
                arrayOf(
                    Exercises.SEATED_OH_DB_PRESS,
                    "https://www.musclehack.com/overhead-dumbbell-press/"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.DB_LAT_RAISES,
                    "https://www.musclehack.com/dumbbell-lateral-raises/"
                ),
                arrayOf(
                    Exercises.LAT_RAISE_MACHINE,
                    "http://www.exrx.net/WeightExercises/DeltoidLateral/LVLateralRaise.html"
                ),
                arrayOf(
                    Exercises.CABLE_LAT_RAISES,
                    "http://www.exrx.net/WeightExercises/DeltoidLateral/CBLateralRaise.html"
                ),
                arrayOf(
                    Exercises.BB_FRONT_RAISES,
                    "http://www.exrx.net/WeightExercises/DeltoidAnterior/BBFrontRaise.html"
                ),
                arrayOf(Exercises.DB_FRONT_RAISES, "https://www.musclehack.com/front-raises/"),
                arrayOf(
                    Exercises.LOW_CABLE_FRONT_RAISES,
                    "http://www.exrx.net/WeightExercises/DeltoidAnterior/CBSeatedFrontRaise.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.BB_SHRUGS,
                    "http://www.exrx.net/WeightExercises/TrapeziusUpper/BBShrug.html"
                ),
                arrayOf(
                    Exercises.SMITH_MACHINE_SHRUGS,
                    "http://www.exrx.net/WeightExercises/TrapeziusUpper/SMShrug.html"
                ),
                arrayOf(
                    Exercises.CABLE_SHRUGS,
                    "https://www.musclehack.com/video-the-best-trapezius-exercise-for-growth/"
                ),
                arrayOf(Exercises.TRAP_BAR_SHRUGS, "https://www.musclehack.com/trap-bar-shrugs/"),
                arrayOf(
                    Exercises.DB_SHRUGS,
                    "http://www.exrx.net/WeightExercises/TrapeziusUpper/DBShrug.html"
                ),
                arrayOf(
                    Exercises.CALF_RAISE_MACHINE_SHRUGS,
                    "http://www.exrx.net/WeightExercises/TrapeziusUpper/SLXGripShrug.html"
                ),
                arrayOf(
                    Exercises.BB_UPRIGHT_ROWS,
                    "http://www.exrx.net/WeightExercises/DeltoidLateral/BBUprightRow.html"
                ),
                arrayOf(
                    Exercises.DB_UPRIGHT_ROWS,
                    "http://www.exrx.net/WeightExercises/DeltoidLateral/DBUprightRow.html"
                ),
                arrayOf(
                    Exercises.UPRIGHT_ROW_ROPE,
                    "https://www.exrx.net/WeightExercises/DeltoidLateral/CBUprightRowRope"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.LEG_RAISES_CAPTAIN,
                    "https://www.musclehack.com/how-to-do-hanging-leg-raises-video/"
                ),
                arrayOf(
                    Exercises.HANGING_LEG_RAISES,
                    "https://www.musclehack.com/how-to-do-hanging-leg-raises-video/"
                ),
                arrayOf(
                    Exercises.HANGING_KNEE_RAISES,
                    "https://www.musclehack.com/how-to-do-hanging-leg-raises-video/"
                ),
                arrayOf(
                    Exercises.DECLINE_SIT_UPS,
                    "https://www.musclehack.com/how-to-build-six-pack-abs/"
                ),
                arrayOf(
                    Exercises.KNEELING_CABLE_CRUNCHES,
                    "https://www.musclehack.com/kneeling-cable-crunches-cable-rope-crunches/"
                ),
                arrayOf(Exercises.REVERSE_CRUNCHES, "https://www.musclehack.com/reverse-crunches/"),
                arrayOf(
                    Exercises.MACHINE_CRUNCHES,
                    "http://www.exrx.net/WeightExercises/RectusAbdominis/LVSeatedCrunch.html"
                ),
                arrayOf(
                    Exercises.MACHINE_LYING_HIP_RAISES,
                    "http://www.exrx.net/WeightExercises/RectusAbdominis/LVLyingLegHipRaise.html"
                )
            ),
            arrayOf(
                arrayOf(
                    Exercises.BB_WRIST_CURLS,
                    "http://www.exrx.net/WeightExercises/WristFlexors/BBWristCurl.html"
                ),
                arrayOf(
                    Exercises.DB_WRIST_CURLS,
                    "https://www.musclehack.com/how-to-get-bigger-forearms-with-this-1-exercise/"
                ),
                arrayOf(
                    Exercises.LOW_CABLE_WRIST_CURLS,
                    "http://www.exrx.net/WeightExercises/WristFlexors/CBWristCurl.html"
                ),
                arrayOf(
                    Exercises.REVERSE_WRIST_CURLS,
                    "http://www.exrx.net/WeightExercises/WristExtensors/BBReverseWristCurl.html"
                ),
                arrayOf(
                    Exercises.WRIST_ROLLER,
                    "http://www.exrx.net/WeightExercises/WristFlexors/CBRollerWristFlexion.html"
                )
            )
        )
        val categories = arrayOf(
            Categories.LEGS_TITLE,
            Categories.LEGS_HAMSTRING_TITLE,
            Categories.LEGS_QUADS_TITLE,
            Categories.CALVES_TITLE,
            Categories.BICEPS_FULL_TITLE,
            Categories.BICEPS_INNER_TITLE,
            Categories.TRICEPS_INNER_TITLE,
            Categories.CHEST_MAIN_TITLE,
            Categories.CHEST_LOWER_TITLE,
            Categories.CHEST_UPPER_TITLE,
            Categories.CHEST_ISOLATION_TITLE,
            Categories.BACK_FULL_TITLE,
            Categories.BACK_SECONDARY_TITLE,
            Categories.BACK_LOWER_TITLE,
            Categories.SHOULDERS_TITLE,
            Categories.SHOULDERS_LATERAL_TITLE,
            Categories.TRAPS_MAIN_TITLE,
            Categories.ABS_TITLE,
            Categories.FOREARMS_TITLE
        )
        for (i in exercises.indices) {
            val group = exercises[i]
            val lst = ArrayList<ExerciseBankItem>()
            for (ex in group) {
                val item = ExerciseBankItem(getStr(ex[0]), ex[1])
                lst.add(item)
                allExercises.add(item)
                allExerciseNames.add(item.exerciseName)
                // link is ex[1]
            }
            val cat = getStr(categories[i])
            this.exercises[cat] = lst
            this.categories.add(cat)
        }
    }

    fun addCustom(customExercises: ArrayList<String>) {
        val category = res.getString(R.string.added_by_user_header)
        /* Organize alphabetically. */
        customExercises.sortWith(Comparator { s1, s2 -> s1.compareTo(s2, ignoreCase = true) })
        val customItems = ArrayList<ExerciseBankItem>()
//        val customItems = customExercises.map { ExerciseBankItem(it, null) }
        for (c in customExercises) {
            val item = ExerciseBankItem(c, null)
            customItems.add(item)
            allExercises.add(item)
        }
        this.exercises[category] = customItems
        this.categories.add(category)
    }

    private fun getStr(name: String): String {
        // String packageName = "com.musclehack.musclehack";
        // Resources res = this.context.getResources();
        val resId = this.res.getIdentifier(name, "string", this.pkg)
        if (resId > 0)
            return this.res.getString(resId) as String
        return ""
    }

    fun getTutorialLink(exerciseName: String): String? {
//        for (cat: ArrayList<String> in exercises!!) {
//            for (exerciseObj in cat) {
//
//            }
//        }
        for ((key, value) in exercises) {
            println("$key = $value")
            for (exBankItem in value) {
                if (exBankItem.exerciseName == exerciseName)
                    return exBankItem.link
            }

        }
        return null
    }

    fun getExerciseBankItem(groupPosition: Int, childPosition: Int): ExerciseBankItem? {
        val category: String = categories[groupPosition]
        val exerciseGroup = exercises[category] ?: return null
        return exerciseGroup[childPosition]
    }

    fun existsInDefault(name: String): Boolean {
        return allExerciseNames.contains(name)
    }


    /*
        fun createTSPA(db: DatabaseHelper, newCycleName: String, newCycleDuration: Int,
                       lowerReps: Int, higherReps: Int) =
                CreateTSPAThread(db, newCycleName, newCycleDuration, lowerReps, higherReps)
                        .start()

        private inner class CreateTSPAThread(val db: DatabaseHelper, val newCycleName: String,
                                             val newCycleDuration: Int, val lowerReps: Int,
                                             val higherReps: Int) : Thread() {

            override fun run() {
                val totalTasks = 71 /* 64 sets, 6 workouts, 1 cycle. */
                var currentTask = 1
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                val cycleID = db.addCycleSQL(newCycleName, newCycleDuration)
                postProgress(currentTask++, totalTasks)
                var workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.monday), 1)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(STANDING_OH_BB_PRESS), lowerReps,
                            higherReps, 180)
                    postProgress(currentTask++, totalTasks)
                }
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(STANDING_OH_DB_PRESS), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(DB_LAT_RAISES), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(DB_FRONT_RAISES), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DB_FRONT_RAISES), lowerReps,
                        higherReps, 240)
                postProgress(currentTask++, totalTasks)
                for (i in 0..2) {
                    db.addSetSQL(cycleID, workoutID, getStr(TRAP_BAR_SHRUGS), 12,
                            15, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(TRAP_BAR_SHRUGS), 12,
                        15, 0)
                postProgress(currentTask++, totalTasks)
                workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.tuesday), 1)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(KNEELING_CABLE_CRUNCHES), 12,
                            15, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(KNEELING_CABLE_CRUNCHES), 12,
                        15, 180)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(DECLINE_SIT_UPS), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(DECLINE_SIT_UPS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(REVERSE_CRUNCHES), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(REVERSE_CRUNCHES), lowerReps,
                        higherReps, 0)
                postProgress(currentTask++, totalTasks)
                workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.wednesday), 1)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DECLINE_TRICEP_EXTENSIONS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DECLINE_TRICEP_EXTENSIONS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(CABLE_TRICEP_PUSHDOWNS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(CABLE_TRICEP_PUSHDOWNS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(CABLE_BENT_OVER_TRICEP_EXTENSIONS), 12,
                        14, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(CABLE_BENT_OVER_TRICEP_EXTENSIONS), 12,
                        14, 180)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(MACHINE_PREACHER_CURLS), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(STANDING_BB_CURLS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(STANDING_BB_CURLS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(EZ_BAR_CURLS), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(SEATED_INCLINE_DB_CURLS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(PINWHEEL_CURLS), 14,
                        16, 0)
                postProgress(currentTask++, totalTasks)
                workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.thursday), 1)
                postProgress(currentTask++, totalTasks)
                for (i in 0..2) {
                    db.addSetSQL(cycleID, workoutID, getStr(DEADLIFT), 4,
                            6, 180)
                    postProgress(currentTask++, totalTasks)
                }
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(SEATED_CABLE_ROWS), lowerReps,
                            higherReps, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(ONE_ARM_DB_ROWS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(ONE_ARM_DB_ROWS), lowerReps,
                        higherReps, 0)
                postProgress(currentTask++, totalTasks)
                workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.friday), 1)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(FLAT_BB_BENCH_PRESS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(FLAT_BB_BENCH_PRESS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(PEC_DECK), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(PEC_DECK), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DEEP_CHEST_DIPS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DEEP_CHEST_DIPS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(INCLINE_BENCH_PRESS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(INCLINE_BENCH_PRESS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(KNEELING_CABLE_CRUNCHES), 12,
                            15, 120)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(KNEELING_CABLE_CRUNCHES), 12,
                        15, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DECLINE_SIT_UPS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(DECLINE_SIT_UPS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(REVERSE_CRUNCHES), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(REVERSE_CRUNCHES), lowerReps,
                        higherReps, 0)
                postProgress(currentTask++, totalTasks)
                workoutID = db.addWorkoutSQL(cycleID, res.getString(R.string.saturday), 1)
                for (i in 0..1) {
                    db.addSetSQL(cycleID, workoutID, getStr(BB_SQUATS), lowerReps,
                            higherReps, 180)
                    postProgress(currentTask++, totalTasks)
                }
                db.addSetSQL(cycleID, workoutID, getStr(LEG_PRESS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(STIFF_LEGGED_DEADLIFTS), 6,
                        8, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(LEG_EXTENSIONS), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(LEG_CURLS), lowerReps,
                        higherReps, 180)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(SEATED_CALF_RAISES), lowerReps,
                        higherReps, 120)
                postProgress(currentTask++, totalTasks)
                db.addSetSQL(cycleID, workoutID, getStr(SEATED_CALF_RAISES), lowerReps,
                        higherReps, 0)
                postProgress(currentTask, totalTasks)
                EventBus.getDefault().post(CycleCreatedEvent(cycleID))
            }
        }

        fun createChestBlast(db: DatabaseHelper, newCycleName: String, newCycleDuration: Int,
                             lowerReps: Int, higherReps: Int) =
                CreateChestBlastThread(db, newCycleName, newCycleDuration, lowerReps, higherReps)
                        .start()


        private inner class CreateChestBlastThread(val db: DatabaseHelper, val newCycleName: String,
                                                   val newCycleDuration: Int, val lowerReps: Int,
                                                   val higherReps: Int) : Thread() {

            override fun run() {
                val totalTasks = 59 /* 56 sets, 3 workouts. */
                val currentTask = 1
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    //            EventBus.getDefault().post(CycleCreatedEvent(cycleID))
            }
        }

        fun createArmsBlast(db: DatabaseHelper, newCycleName: String, newCycleDuration: Int,
                            lowerReps: Int, higherReps: Int) =
                CreateArmsBlastThread(db, newCycleName, newCycleDuration, lowerReps, higherReps)
                        .start()


        private inner class CreateArmsBlastThread(val db: DatabaseHelper, val newCycleName: String,
                                                  val newCycleDuration: Int, val lowerReps: Int,
                                                  val higherReps: Int) : Thread() {

            override fun run() {
                val totalTasks = 59 /* 56 sets, 3 workouts. */
                val currentTask = 1
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    //            EventBus.getDefault().post(CycleCreatedEvent(cycleID))
            }
        }

        fun createLegsBlast(db: DatabaseHelper, newCycleName: String, newCycleDuration: Int,
                            lowerReps: Int, higherReps: Int) =
                CreateLegsBlastThread(db, newCycleName, newCycleDuration, lowerReps, higherReps)
                        .start()


        private inner class CreateLegsBlastThread(val db: DatabaseHelper, val newCycleName: String,
                                                  val newCycleDuration: Int, val lowerReps: Int,
                                                  val higherReps: Int) : Thread() {

            override fun run() {
                val totalTasks = 59 /* 56 sets, 3 workouts. */
                val currentTask = 1
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    //            EventBus.getDefault().post(CycleCreatedEvent(cycleID))
            }
        }
        */
}
