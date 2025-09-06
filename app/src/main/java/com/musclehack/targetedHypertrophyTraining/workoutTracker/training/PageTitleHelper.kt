package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.content.Context
import com.musclehack.targetedHypertrophyTraining.R
import javax.inject.Inject

/** Util class that create the page title for Log Note and for our Workout Tracker. */
class PageTitleHelper @Inject constructor(private val appContext: Context) {
    fun getPageTitleForNote(day: Int, frequency: Int, workoutName: String): String {
        val title = appContext.getString(R.string.tabs_day_header)
        val weekNum = if (frequency > 1) "${day / frequency + 1}" else "${day + 1}"
        return if (frequency > 1) {
            // Monday and Friday workout
            /*if (workoutName == ctxt.resources.getString(R.string.monday_and_friday)) {
                if (day % workoutRepeats == 0)
                    String.format(title, "$weekNum (${ctxt.resources.getString(R.string.monday_day_name)})")
                else
                    String.format(title, "$weekNum (${ctxt.resources.getString(R.string.friday_day_name)})")
            } else */
            String.format(title, "$weekNum (Day ${day % frequency + 1} of $frequency)")

        } else String.format(title, weekNum)
    }

    fun getPageTitleForTab(day: Int, frequency: Int, workoutName: String): CharSequence {
        val title = appContext.getString(R.string.tabs_day_header)
        val weekNum = if (frequency > 1) "${day / frequency + 1}" else "${day + 1}"
        return if (frequency > 1) {
            // Monday and Friday workout
            if (workoutName == appContext.resources.getString(R.string.monday_and_friday)) {
                if (day % frequency == 0)
                    String.format(
                        title,
                        "$weekNum (${appContext.resources.getString(R.string.monday_day_name)})"
                    )
                else
                    String.format(
                        title,
                        "$weekNum (${appContext.resources.getString(R.string.friday_day_name)})"
                    )
            } else
                String.format(title, "$weekNum (Day ${day % frequency + 1} of ${frequency})")

        } else String.format(title, weekNum)
    }
}