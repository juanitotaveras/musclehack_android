package com.musclehack.targetedHypertrophyTraining

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerChangeEvent
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerEventType

private const val TAG = "TimerReceiver"

class TimerReceiver : BroadcastReceiver() {
    private val timerChangeData = MutableLiveData<Event<TimerChangeEvent>>()

    companion object {
        const val TIMER_CHANGE_ACTION = "timerChangeAction"
        const val REMAINING_TIME_KEY = "remainingTime"
        const val EVENT_TYPE_KEY = "timerEventType"
    }

    fun getData(): LiveData<Event<TimerChangeEvent>> = timerChangeData
    override fun onReceive(context: Context?, intent: Intent?) {
        val remainingTime = intent?.getIntExtra(REMAINING_TIME_KEY, 0) ?: 0
        val eType = intent?.getStringExtra(EVENT_TYPE_KEY) ?: TimerEventType.TICK.toString()
        Log.i(TAG, "onReceive called. remainingTime: $remainingTime")
        timerChangeData.value =
            Event(TimerChangeEvent(newTime = remainingTime, type = TimerEventType.valueOf(eType)))
    }
}