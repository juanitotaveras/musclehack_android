package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

/**
 * Used to hold the data for determining the previous stats relative to your current
 * exercise set.
 */
data class PrevSetData(val prevWeight: Double?, val prevReps: Int?, val prevNoteDay: Int?)