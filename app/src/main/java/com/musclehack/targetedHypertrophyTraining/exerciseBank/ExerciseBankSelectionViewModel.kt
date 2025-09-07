package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.ExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.exerciseBank.entities.UserExercise
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseBankSelectionViewModel @Inject constructor(
    private val exerciseBankRepository: ExerciseBankRepository,
    private val appContext: Context
) : ViewModel(), ExerciseBankViewModel {

    private val _defaultExercises = MutableLiveData<DefaultExercises>()
    private val defaultExercises: LiveData<DefaultExercises> = _defaultExercises
    private val _bankSearchText = MutableLiveData("")
    private val bankSearchText: LiveData<String> = _bankSearchText
    private val _filteredItems = MutableLiveData<List<ExerciseBankItem>>()
    private val filteredItems: LiveData<List<ExerciseBankItem>> = _filteredItems

    /** This will be true if we are in TrianingPage, false if we are coming from
     * CreateNewSet. */
    private var isExerciseBankOverlayActivity = false

    /** Events */
    private val _openExerciseLinkEvent = MutableLiveData<Event<String>>()
    val openExerciseLinkEvent: LiveData<Event<String>> = _openExerciseLinkEvent
    private val _exerciseChosenEvent = MutableLiveData<Event<String>>()
    val exerciseChosenEvent: LiveData<Event<String>> = _exerciseChosenEvent

    private val openExerciseCategoryIndices = HashSet<Int>()

    private var userExercises: List<UserExercise> = ArrayList()

    /** Initialize viewModel data. */
    fun start(hostActivity: FragmentActivity?) {
        viewModelScope.launch {
            // TODO: Consider making repository return Result object
            val defaultExercisesResult = exerciseBankRepository.getDefaultExercises()
            val userExercisesResult = exerciseBankRepository.getUserExercises()
            if (userExercisesResult is Result.Success) {
                userExercises = userExercisesResult.data
            }
            _defaultExercises.value = defaultExercisesResult
        }
        isExerciseBankOverlayActivity = hostActivity is ExerciseBankOverlayActivity
    }

    fun setBankSearchText(st: String) {
        _bankSearchText.value = st
        val matchingItems = getMatchingExercises(st)
        if (matchingItems != null) {
            _filteredItems.value = matchingItems
        }
    }

    private fun onDefaultExerciseClicked(groupPosition: Int, childPosition: Int) {
        /* Highlight exercise and have check on upper right corner */
        defaultExercises.value?.getExerciseBankItem(groupPosition, childPosition)?.let { item ->
            _exerciseChosenEvent.value = Event(item.exerciseName)
        }
    }

    private fun onUserExerciseClicked(exPosition: Int) {
        if (exPosition < userExercises.size) {
            _exerciseChosenEvent.value = Event(userExercises[exPosition].name)
        }
    }

    override fun isExerciseBankOverlayActivity(): Boolean {
        return isExerciseBankOverlayActivity
    }

    override fun getBankSearchText(): LiveData<String> {
        return bankSearchText
    }

    override fun onUserExerciseDeleteClicked(exercisePosition: Int) {
        // throw exception. Button should not be visible
    }

    override fun onFilteredExerciseClicked(position: Int) {
        val item: ExerciseBankItem = filteredItems.value?.get(position) ?: return
        _exerciseChosenEvent.value = Event(item.exerciseName)
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
        if (!isExerciseBankOverlayActivity) {
            if (isExpanding) {
                insertOpenExerciseCategoryIndex(groupPosition)
            } else {
                removeOpenExerciseCategoryIndex(groupPosition)
            }
        }
    }

    override fun getAppContext() = appContext

    override fun isUserCreatedCategory(groupPosition: Int): Boolean {
        val df = defaultExercises.value ?: return false
        return groupPosition == df.categories.size
    }

    override fun onExerciseClicked(groupPosition: Int, childPosition: Int) {
        if (isUserCreatedCategory(groupPosition)) {
            onUserExerciseClicked(childPosition)
        } else {
            onDefaultExerciseClicked(groupPosition, childPosition)
        }
    }

    override fun getExerciseDeletedEvent(): LiveData<Event<Int>> {
        // This should be inactive because we have no
        // delete functionality here.
        return MutableLiveData<Event<Int>>()
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