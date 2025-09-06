package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.app.Activity

abstract class CycleTemplate(
    val name: String, val numWeeks: Int, val lowerReps: Int?,
    val higherReps: Int?, val host: Activity
) {

    abstract suspend fun create(name: String, numWeeks: Int, lowerReps: Int?, higherReps: Int?)
    private val postProgressDelegate: ProgressPostedListener? = host as? CreatingCycleActivity

    fun getStr(name: String): String {
        // String packageName = "com.musclehack.musclehack";
        // Resources res = this.context.getResources();
        val ctxt = host.applicationContext
        val resId = ctxt.resources.getIdentifier(
            name,
            "string", host.applicationContext.packageName
        )
        if (resId > 0)
            return ctxt.getString(resId) as String
        return ""
    }


    fun postProgress(currentTask: Int, totalTasks: Int) {
        val progress = currentTask * 100 / totalTasks
        postProgressDelegate?.onProgressPosted(percentage = progress)
    }

    fun cycleCreated(cycleID: Int) {
        postProgressDelegate?.onCycleCreated(cycleID = cycleID)
    }
}