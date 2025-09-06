package com.musclehack.targetedHypertrophyTraining.data.repository

/**
 * Interface to data layer.
 */
interface CycleCreationRepository {
    suspend fun createThreeDayFull(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    )

    suspend fun createBlank(newCycleName: String, numWeeks: Int)
    suspend fun createThreeDaySplit(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    )

    suspend fun createFiveDaySplit(
        newCycleName: String, newCycleDuration: Int,
        lowerReps: Int, higherReps: Int
    )
}