package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseLog
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.PrevSetData

// TODO: Rename to LogCardPresenter
data class LogCardModel(
    val exerciseLog: ExerciseLog,
    val exerciseSet: ExerciseSet,
    var prevSetData: PrevSetData
) {
    fun getWeightText(): String {
        val weight = exerciseLog.weight ?: return ""

        return weight.toString()
    }

    fun getRepsText(): String {
        val reps = exerciseLog.reps ?: return ""

        return reps.toString()
    }

    /** Used to display exercise name in EditSets. */
    fun getExerciseNameDefaultText() = "${exerciseSet.position + 1}. ${exerciseSet.exerciseName}"

    /** Will return substitution name if available, else returns default name. */
    fun getExerciseName(): String {
        val subName = exerciseLog.subName
        if (subName != null && subName.isNotEmpty()) {
            return subName
        }
        return exerciseSet.exerciseName
    }

    fun getExerciseNumText() = "${(exerciseSet.position + 1)}."

    fun getLowerReps() = exerciseSet.lowerReps.toString()

    fun getHigherReps() = exerciseSet.higherReps.toString()

    fun shouldShowPrevWeight(): Boolean {
        return prevSetData.prevWeight != null && exerciseLog.day > 0
    }

    fun getPrevWeight(): String {
        if (!shouldShowPrevWeight()) return ""

        return prevSetData.prevWeight.toString()
    }

    fun shouldShowPrevReps(): Boolean {
        return prevSetData.prevReps != null && exerciseLog.day > 0
    }

    fun getPrevReps(): String {
        if (!shouldShowPrevReps()) return ""

        return prevSetData.prevReps.toString()
    }

    fun isLastSet(viewModel: TrainingViewModel): Boolean {
        return exerciseSet.position == viewModel.getSetCount() - 1
    }

    var isFocused = false

    fun hasEqualContents(other: LogCardModel): Boolean {
        return exerciseLog.hasEqualContents(other.exerciseLog)
                && exerciseSet.hasEqualContents(other.exerciseSet)
                && prevSetData.prevReps == other.prevSetData.prevReps
                && prevSetData.prevWeight == other.prevSetData.prevWeight
                && prevSetData.prevNoteDay == other.prevSetData.prevNoteDay
    }
}