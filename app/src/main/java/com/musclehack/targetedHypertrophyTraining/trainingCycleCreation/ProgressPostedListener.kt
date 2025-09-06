package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

interface ProgressPostedListener {
    fun onProgressPosted(percentage: Int)
    fun onCycleCreated(cycleID: Int)
}