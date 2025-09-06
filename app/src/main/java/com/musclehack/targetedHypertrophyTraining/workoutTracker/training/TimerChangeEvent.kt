package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

class TimerChangeEvent(val newTime: Int, val type: TimerEventType)

/** Status updates broadcasted by the Timer Service.*/
enum class TimerEventType {
    START, STOP, PAUSE, RESUME, TICK, FINISH
}

/** Commands sent from UI to the Timer Service. */
enum class TimerCommandType {
    START, STOP, PAUSE, RESUME
}
