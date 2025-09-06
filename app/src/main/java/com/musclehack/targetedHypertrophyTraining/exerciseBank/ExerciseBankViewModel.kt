package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.Context
import androidx.lifecycle.LiveData
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise

interface ExerciseBankViewModel {
    fun onFilteredExerciseClicked(position: Int)
    fun getFilteredItems(): LiveData<List<ExerciseBankItem>>
    fun getDefaultExercises(): LiveData<DefaultExercises>
    fun getUserExercises(): List<UserExercise>
    fun onExerciseCategoryClicked(groupPosition: Int, isExpanding: Boolean)
    fun getAppContext(): Context
    fun isExerciseBankOverlayActivity(): Boolean
    fun getBankSearchText(): LiveData<String>
    fun onUserExerciseDeleteClicked(exercisePosition: Int)
    fun isUserCreatedCategory(groupPosition: Int): Boolean
    fun onExerciseClicked(groupPosition: Int, childPosition: Int)
    fun getExerciseDeletedEvent(): LiveData<Event<Int>>
}