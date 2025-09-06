package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.ExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import com.musclehack.targetedHypertrophyTraining.notifyObserver
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOW_DELETE_EXERCISE_BANK_ITEM_WARNING
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/** This view model is only used for our Exercise Bank tab. */
class ExerciseBankMainViewModel @Inject constructor(
    private val exerciseBankRepository: ExerciseBankRepository,
    private val appContext: Context,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), ExerciseBankViewModel {

    private val _defaultExercises = MutableLiveData<DefaultExercises>()
    private val defaultExercises: LiveData<DefaultExercises> = _defaultExercises
    private val _bankSearchText = MutableLiveData("")
    private val bankSearchText: LiveData<String> = _bankSearchText
    private val _filteredItems = MutableLiveData<List<ExerciseBankItem>>()
    private val filteredItems: LiveData<List<ExerciseBankItem>> = _filteredItems
    /** This will be false if we are in the Exercise Bank tab,
     * true if this is displayed over a different tab. */

    /** Events */
    private val _openExerciseLinkEvent = MutableLiveData<Event<String>>()
    val openExerciseLinkEvent: LiveData<Event<String>> = _openExerciseLinkEvent

    private val openExerciseCategoryIndices = HashSet<Int>()
    private var userExercises: List<UserExercise> = ArrayList()

    private val _showConfirmDeleteExerciseEvent = MutableLiveData<Event<String>>()
    val showConfirmDeleteExerciseEvent: LiveData<Event<String>> = _showConfirmDeleteExerciseEvent

    private val _exerciseDeletedEvent = MutableLiveData<Event<Int>>()

    /** Initialize viewModel data. */
    fun start() {
        viewModelScope.launch {
            // TODO: Consider making repository return Result object
            val defaultExercisesResult = exerciseBankRepository.getDefaultExercises()
            val userExercisesResult = exerciseBankRepository.getUserExercises()
            if (userExercisesResult is Result.Success) {
                userExercises = userExercisesResult.data
            }
            _defaultExercises.value = defaultExercisesResult
        }
    }

    fun setBankSearchText(st: String) {
        _bankSearchText.value = st
        val matchingItems = getMatchingExercises(st)
        if (matchingItems != null) {
            _filteredItems.value = matchingItems
        }
    }

    private fun onDefaultExerciseClicked(groupPosition: Int, childPosition: Int) {
        if (!isUserCreatedCategory(groupPosition)) {
            defaultExercises.value?.getExerciseBankItem(
                groupPosition,
                childPosition
            )?.link?.let { link ->
                _openExerciseLinkEvent.value = Event(link)
            }
        }
    }

    private fun onUserExerciseClicked(exPosition: Int) {
        // do nothing. We have no tutorial to show.
    }

    override fun onExerciseClicked(groupPosition: Int, childPosition: Int) {
        if (isUserCreatedCategory(groupPosition)) {
            onUserExerciseClicked(childPosition)
        } else {
            onDefaultExerciseClicked(groupPosition, childPosition)
        }
    }

    override fun getExerciseDeletedEvent(): LiveData<Event<Int>> {
        return _exerciseDeletedEvent
    }

    override fun isExerciseBankOverlayActivity() = false

    override fun getBankSearchText() = bankSearchText
    override fun onUserExerciseDeleteClicked(exercisePosition: Int) {
        // check prefs to see if we should show warning.
        // Show dialog to confirm deleting exercise.
        val exerciseName = userExercises[exercisePosition].name
        if (sharedPreferences.getBoolean(PREF_SHOW_DELETE_EXERCISE_BANK_ITEM_WARNING, true)) {
            _showConfirmDeleteExerciseEvent.value = Event(exerciseName)
        } else {
            // delete exercise without showing dialog
            deleteCustomExercise(exerciseName)
        }
    }

    fun deleteCustomExercise(exerciseName: String) {
        viewModelScope.launch {
            val result = exerciseBankRepository.deleteUserExercise(exerciseName)
            if (result is Result.Success) {
                // show success snackbar
                // also refresh list
                val userExercisesResult = exerciseBankRepository.getUserExercises()
                if (userExercisesResult is Result.Success) {
                    userExercises = userExercisesResult.data
                }
                _exerciseDeletedEvent.postValue(Event(0))
            }
        }
    }

    override fun onFilteredExerciseClicked(position: Int) {
        val item: ExerciseBankItem? = filteredItems.value?.get(position)
        if (item == null) {
            // log stuff
            return
        }
        val tutorialLink = defaultExercises.value?.getTutorialLink(item.exerciseName)
        if (tutorialLink == null) {
            // message UI
            return
        }
        _openExerciseLinkEvent.value = Event(tutorialLink)
    }

    override fun getFilteredItems(): LiveData<List<ExerciseBankItem>> {
        return filteredItems
    }

    override fun getDefaultExercises(): LiveData<DefaultExercises> {
        return defaultExercises
    }

    override fun getUserExercises(): List<UserExercise> {
        return userExercises
    }

    override fun onExerciseCategoryClicked(groupPosition: Int, isExpanding: Boolean) {
        if (isExpanding) {
            insertOpenExerciseCategoryIndex(groupPosition)
        } else {
            removeOpenExerciseCategoryIndex(groupPosition)
        }
    }

    override fun getAppContext(): Context {
        return appContext
    }

    override fun isUserCreatedCategory(groupPosition: Int): Boolean {
        val df = defaultExercises.value ?: return false
        return groupPosition == df.categories.size
    }

    fun getMatchingExercises(searchTxt: String): List<ExerciseBankItem>? {
        return defaultExercises.value?.allExercises?.filter {
            it.exerciseName.contains(searchTxt, ignoreCase = true)
        }
    }

    /** Save our previously opened exercise categories to restore later. */

    fun getOpenExerciseCategoryIndices(): HashSet<Int> {
        return openExerciseCategoryIndices
    }

    private fun insertOpenExerciseCategoryIndex(idx: Int) {
        openExerciseCategoryIndices.add(idx)
    }

    private fun removeOpenExerciseCategoryIndex(idx: Int) {
        openExerciseCategoryIndices.remove(idx)
    }
}