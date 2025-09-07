package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_STARTS_SINCE_ASK_RATE
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_USER_RATED
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_WORKOUT_TRACKER_STATE
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import kotlinx.coroutines.launch
import javax.inject.Inject


class TrackerHomeViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val preferences: SharedPreferences
) : ViewModel() {

    val _cycles: LiveData<List<Cycle>> = trackerRepository.observeCycles()

    private val _currentSelectedCycle = MutableLiveData<Cycle?>(null)

    private val _openCycleEvent = MutableLiveData<Event<Long>>()
    val openCycleEvent: LiveData<Event<Long>> = _openCycleEvent

    private val _bottomSheetItemSelectedEvent = MutableLiveData<Event<BottomSheetMenuOption>>()
    val bottomSheetItemSelectedEvent: LiveData<Event<BottomSheetMenuOption>> =
        _bottomSheetItemSelectedEvent

    private val _openBottomSheetEvent = MutableLiveData<Event<Int>>()
    val openBottomSheetEvent: LiveData<Event<Int>> = _openBottomSheetEvent

    private val _dismissBottomSheetEvent = MutableLiveData<Event<Int>>()
    val dismissBottomSheetEvent: LiveData<Event<Int>> = _dismissBottomSheetEvent

    /**
     * Called when a cycleOption button is clicked.
     */
    fun onCycleOptionsButtonClicked(pos: Int) {
        val cycle = _cycles.value?.get(pos)
        cycle?.let {
            _currentSelectedCycle.value = it
            _openBottomSheetEvent.value = Event(0)
        }
    }

    fun empty(): LiveData<Boolean> {
        return _cycles.map { cycles ->
            cycles.isEmpty()
        }
    }

    fun getSelectedCycle(): Cycle? {
        return _currentSelectedCycle.value
    }

    fun onConfirmDeleteCycle() {
        _currentSelectedCycle.value?.let {
            viewModelScope.launch { trackerRepository.deleteCycle(it) }
        }
    }

    fun onConfirmChangeCycleDuration(newDuration: Int) {
        _currentSelectedCycle.value?.let {
            // cycleID, oldDuration, newDuration
            viewModelScope.launch { trackerRepository.changeCycleDuration(it, newDuration) }
        }
    }

    fun onConfirmRenameCycle(newName: String) {
        _currentSelectedCycle.value?.let {
            viewModelScope.launch {
                trackerRepository.changeCycleName(it, newName)
            }
        }
    }

    fun sortCyclesByDateUsed() {
        viewModelScope.launch {
            trackerRepository.sortCyclesByDateUsed()
        }
    }

    fun sortCyclesByDateCreated() {
        viewModelScope.launch {
            trackerRepository.sortCyclesByDateCreated()
        }
    }

    fun onDismissBottomSheet() {
        _dismissBottomSheetEvent.value = Event(0)
    }

    fun cloneCycle() {
        _currentSelectedCycle.value?.let {
            viewModelScope.launch {
                trackerRepository.cloneCycle(it.id)
            }
        }
    }

    fun shouldAskUserToRateApp(): Boolean {
        val startsSinceAsk: Int = preferences.getInt(PREF_STARTS_SINCE_ASK_RATE, 0)
        val hasRated: Boolean = preferences.getBoolean(PREF_USER_RATED, false)
        var shouldAskUserToRate = false
        if (!hasRated) {
            if (startsSinceAsk >= 5) {  // if it's been more than 1 week since ask
                shouldAskUserToRate = true
                preferences.edit().putInt(PREF_STARTS_SINCE_ASK_RATE, 0).apply()
            }
        }
        return shouldAskUserToRate
    }

    /**
     * Called by Data Binding.
     */
    fun openCycle(cycleId: Long) {
        preferences.edit().putString(PREF_WORKOUT_TRACKER_STATE, cycleId.toString()).apply()
        _openCycleEvent.value = Event(cycleId)
    }

    /**
     * Called by BottomSheet data binding.
     */
    fun onRenameCycleClicked() {
        _bottomSheetItemSelectedEvent.value = Event(BottomSheetMenuOption.RENAME_CYCLE)
    }

    fun onChangeCycleDurationClicked() {
        _bottomSheetItemSelectedEvent.value = Event(BottomSheetMenuOption.CHANGE_CYCLE_DURATION)
    }

    fun onDeleteCycleClicked() {
        _bottomSheetItemSelectedEvent.value = Event(BottomSheetMenuOption.DELETE_CYCLE)
    }

    fun onCloneCycleClicked() {
        _bottomSheetItemSelectedEvent.value = Event(BottomSheetMenuOption.CLONE_CYCLE)
    }

    enum class BottomSheetMenuOption {
        RENAME_CYCLE,
        CHANGE_CYCLE_DURATION,
        DELETE_CYCLE,
        CLONE_CYCLE
    }
}