package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.notifyObserver
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_WORKOUT_TRACKER_STATE
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackerWorkoutsViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val appContext: Context,
    private val preferences: SharedPreferences
) : ViewModel() {

    private val _cycleId = MutableLiveData<Long>()

    private val _currentlySelectedWorkout = MutableLiveData<Workout?>(null)
    val currentlySelectedWorkout: LiveData<Workout?> = _currentlySelectedWorkout

    private val _trackerWorkoutsSortMode = MutableLiveData(false)
    val trackerWorkoutsSortMode: LiveData<Boolean> = _trackerWorkoutsSortMode
    var justMovedPosition: Int? = null

    /** Events */
    private val _openWorkoutEvent = MutableLiveData<Event<Long>>()
    val openWorkoutEvent: LiveData<Event<Long>> = _openWorkoutEvent
    private val _positionPlusOneClickedEvent = MutableLiveData<Event<Int>>()
    val positionPlusOneClickedEvent: LiveData<Event<Int>> = _positionPlusOneClickedEvent
    private val _positionMinusOneClickedEvent = MutableLiveData<Event<Int>>()
    val positionMinusOneClickedEvent: LiveData<Event<Int>> = _positionMinusOneClickedEvent

    private val _openBottomSheetEvent = MutableLiveData<Event<Int>>()
    val openBottomSheetEvent: LiveData<Event<Int>> = _openBottomSheetEvent
    private val _dismissBottomSheetEvent = MutableLiveData<Event<Int>>()
    val dismissBottomSheetEvent: LiveData<Event<Int>> = _dismissBottomSheetEvent
    private val _bottomSheetOptionSelectedEvent =
        MutableLiveData<Event<TrackerWorkoutsBottomSheetDialogFragment
        .TrackerWorkoutsBottomSheetOption>>()
    val bottomSheetOptionSelectedEvent:
            LiveData<Event<TrackerWorkoutsBottomSheetDialogFragment
            .TrackerWorkoutsBottomSheetOption>> = _bottomSheetOptionSelectedEvent

    private val _cycle = MediatorLiveData<Cycle>()
    val cycle: LiveData<Cycle> = _cycle

    private val _workouts: MutableLiveData<List<Workout>?> = cycle.switchMap { cycle ->
        trackerRepository.observeWorkouts(cycle.id).map { workoutsResult ->
            if (workoutsResult is Result.Success) workoutsResult.data else null
        }
    } as MutableLiveData<List<Workout>?>

    val workouts: LiveData<List<Workout>?> = _workouts

    fun start(cycleId: Long?) {
        if (cycleId == _cycleId.value) return

        // trigger load
        _cycleId.value = cycleId
    }

    init {
        _cycle.addSource(_cycleId) { id ->
            viewModelScope.launch {
                val cycleResult = trackerRepository.getCycleById(id)
                if (cycleResult is Result.Success) {
                    _cycle.value = cycleResult.data
                } else {
                    // log some error
                }
            }
        }
    }

    fun turnOnSortMode() {
        // clear out old data
        justMovedPosition = null
        _trackerWorkoutsSortMode.value = true

    }

    fun confirmNewOrder() {
        // save our new positions
        workouts.value?.let { workouts ->
            viewModelScope.launch {
                trackerRepository.saveNewWorkoutPositions(workouts)
            }
        }
        _trackerWorkoutsSortMode.value = false
    }

    fun cancelSortMode() {
        // revert positions
        _cycleId.notifyObserver()
        _trackerWorkoutsSortMode.value = false
    }

    fun positionPlusOneClicked(position: Int) {
        val ws: MutableList<Workout> = (_workouts.value ?: return).toMutableList()
        val t = ws[position]
        ws[position] = ws[position + 1]
        ws[position + 1] = t
        recomputePositions(ws)
        // Used for scrolling when we changed top item
        _workouts.value = ws
        _positionPlusOneClickedEvent.value = Event(position)
    }

    fun positionMinusOneClicked(position: Int) {
        val ws: MutableList<Workout> = (_workouts.value ?: return).toMutableList()
        val t = ws[position]
        ws[position] = ws[position - 1]
        ws[position - 1] = t
        recomputePositions(ws)
        // Used for scrolling when we changed top item
        _workouts.value = ws
        _positionMinusOneClickedEvent.value = Event(position)
    }

    /** Changes position variable in Workout objects to match position in list. */
    private fun recomputePositions(workouts: MutableList<Workout>) {
        workouts.forEachIndexed { index, workout ->
            workout.position = index
        }
    }

    fun onCreateNewWorkout(workout: Workout) {
        val cId = _cycleId.value
        if (cId != null) {
            workout.cycleId = cId.toLong()
        }
        // update position
        cycle.value?.let { cycle ->
            workout.position = workouts.value?.count() ?: 1
        }
        viewModelScope.launch {
            trackerRepository.insertWorkout(workout)
        }
    }

    fun onWorkoutRenamed(newWorkoutName: String) {
        _currentlySelectedWorkout.value?.let { workout ->
            viewModelScope.launch {
                trackerRepository.updateWorkoutName(workout, newWorkoutName)
            }
        }
    }

    fun onWorkoutDeleted() {
        currentlySelectedWorkout.value?.let { workout ->
            viewModelScope.launch {
                trackerRepository.deleteWorkout(workout)
                _currentlySelectedWorkout.value = null
            }
        }
    }

    /** For Bottom Sheet */

    fun onRenameWorkoutOptionSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(TrackerWorkoutsBottomSheetDialogFragment.TrackerWorkoutsBottomSheetOption.RENAME)
    }

    fun onDeleteWorkoutOptionSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(TrackerWorkoutsBottomSheetDialogFragment.TrackerWorkoutsBottomSheetOption.DELETE)
    }

    fun onDismissBottomSheet() {
        _dismissBottomSheetEvent.value = Event(0)
    }

    /**
     * Called by Data Binding.
     */
    fun openWorkout(workoutId: Long) {
        val cycleId = _cycleId.value
        if (cycleId != null) {
            preferences.edit().putString(PREF_WORKOUT_TRACKER_STATE, "$cycleId-$workoutId").apply()
        }
        _openWorkoutEvent.value = Event(workoutId)
    }

    fun onWorkoutOptionsClicked(position: Int) {
        _currentlySelectedWorkout.value = workouts.value?.get(position)
        _openBottomSheetEvent.value = Event(position)
    }

    // Util methods
    fun getColor(resID: Int) = ContextCompat.getColor(appContext, resID)

    fun workoutsCount(): Int {
        return workouts.value?.count() ?: 0
    }
}