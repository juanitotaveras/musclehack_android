package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.res.Resources
import android.os.Process

/**
 * Created by juanito on 3/11/2018.
 */
class DeleteCustomExerciseThread
    (
    val res: Resources, val packageName: String,
    private val exerciseName: String
) : Thread() {

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        /*
        val success = d.deleteCustomExerciseSQL(exerciseName)
        if (success) {
            //        Log.e(getClass().getName(), "deleted exercise successfully");
            val defaultExercises = DefaultExercises(res, packageName)
            val custom = d.customExercises
            if (custom.size > 0)
                defaultExercises.addCustom(custom)

            EventBus.getDefault().post(DefaultExercisesLoadedEvent(defaultExercises))
        } else
            Log.e(javaClass.name, "FAILED TO DELETE EXERCISE " + exerciseName)*/
    }
}