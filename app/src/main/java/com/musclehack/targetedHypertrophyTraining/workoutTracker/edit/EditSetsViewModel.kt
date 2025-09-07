package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.ExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.exerciseBank.DefaultExercises
import com.musclehack.targetedHypertrophyTraining.notifyObserver
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.TrainingPagerData
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditSetsViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val exerciseBankRepository: ExerciseBankRepository,
    val context: Context
) : ViewModel() {
    @Inject
    lateinit var appContext: Context
    val args: MutableLiveData<TrainingPagerData> = MutableLiveData()
    private val _exerciseSetsSource: LiveData<List<ExerciseSet>> = args.switchMap { tData ->
        if (tData != null)
            trackerRepository.getExerciseSets(tData.first.id, tData.second.id)
        else MutableLiveData()
    }

    val exerciseSetsMediator = MediatorLiveData<List<ExerciseSet>>()

    /** Used for EditSetsFragment. */
    private val _editSetsSortMode = MutableLiveData(false)
    val editSetsSortMode: LiveData<Boolean> = _editSetsSortMode
    var justMovedPosition: Int? = null

    /** Used by our CreateNewSetFragment */
    private val _selectedExerciseNameText = MutableLiveData<String>()
    val selectedExerciseNameText: LiveData<String> = _selectedExerciseNameText

    /** Used by our bottom sheet. */
    private val _currentlySelectedExerciseSet = MutableLiveData<ExerciseSet?>(null)
    val currentlySelectedExerciseSet: LiveData<ExerciseSet?> = _currentlySelectedExerciseSet

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    /** Events */
    private val _positionPlusOneClickedEvent = MutableLiveData<Event<Int>>()
    val positionPlusOneClickedEvent: LiveData<Event<Int>> = _positionPlusOneClickedEvent
    private val _positionMinusOneClickedEvent = MutableLiveData<Event<Int>>()
    val positionMinusOneClickedEvent: LiveData<Event<Int>> = _positionMinusOneClickedEvent
    private val _openBottomSheetEvent = MutableLiveData<Event<Int>>()
    val openBottomSheetEvent: LiveData<Event<Int>> = _openBottomSheetEvent
    private val _dismissBottomSheetEvent = MutableLiveData<Event<Int>>()
    val dismissBottomSheetEvent: LiveData<Event<Int>> = _dismissBottomSheetEvent
    private val _bottomSheetOptionSelectedEvent =
        MutableLiveData<Event<EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption>>()
    val bottomSheetOptionSelectedEvent:
            LiveData<Event<EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption>> =
        _bottomSheetOptionSelectedEvent

    private val _modelLoadedEvent = MutableLiveData<Event<Int>>()
    val modelLoadedEvent: LiveData<Event<Int>> = _modelLoadedEvent

    /** Default exercises */
    private val df by lazy {
        DefaultExercises(appContext.resources, appContext.packageName)
    }


    /**
     * Called immediately after ViewModel is created.
     * Would be nice for this to be a constructor.
     */
    fun start(cycleId: Long, workoutId: Long) {
        // trigger load
        viewModelScope.launch {
            getCycleAndWorkoutData(cycleId, workoutId)?.let { tData ->
                args.value = tData
                _modelLoadedEvent.value = Event(0)
            }
        }
    }

    init {
        exerciseSetsMediator.addSource(_exerciseSetsSource) {
            exerciseSetsMediator.value = it
        }
        exerciseSetsMediator.addSource(editSetsSortMode) {
            // do nothing
            exerciseSetsMediator.value = exerciseSetsMediator.value
        }
    }

    /**
     * Database calls
     */
    private suspend fun getCycleAndWorkoutData(cycleId: Long, workoutId: Long): TrainingPagerData? {
        val result: Result<TrainingPagerData> = trackerRepository
            .getTrainingPagerData(cycleId, workoutId)
        if (result is Result.Success) {
            return result.data
        }
        // log error to UI here
        return null
    }

    fun confirmNewOrder() {
        // clear out old data
        justMovedPosition = null
        _editSetsSortMode.value = true
    }

    fun turnOffSortMode() {
        // Save our new positions
        exerciseSetsMediator.value?.let { eSets ->
            viewModelScope.launch {
                trackerRepository.saveNewSetPositions(eSets)
            }
        }
        _editSetsSortMode.value = false
    }

    fun cancelSortMode() {
        // revert positions
        args.notifyObserver()
        _editSetsSortMode.value = false
    }

    fun positionPlusOneClicked(position: Int) {
        val exSets: MutableList<ExerciseSet> = (exerciseSetsMediator.value
            ?: return).toMutableList()
        val t = exSets[position]
        exSets[position] = exSets[position + 1]
        exSets[position + 1] = t
        recomputePositions(exSets)
        // Used for scrolling when we changed top item
        _positionPlusOneClickedEvent.value = Event(position)
        exerciseSetsMediator.value = exSets
    }

    fun positionMinusOneClicked(position: Int) {
        val exSets: MutableList<ExerciseSet> = (exerciseSetsMediator.value
            ?: return).toMutableList()
        val t = exSets[position]
        exSets[position] = exSets[position - 1]
        exSets[position - 1] = t
        recomputePositions(exSets)
        // Used for scrolling when we changed top item
        _positionMinusOneClickedEvent.value = Event(position)
        exerciseSetsMediator.value = exSets
    }

    /** Changes position variable in ExerciseSets objects to match position in list. */
    private fun recomputePositions(exSets: MutableList<ExerciseSet>) {
        exSets.forEachIndexed { index, exerciseSet ->
            exerciseSet.position = index
        }
    }

    fun onChangeSetExercise(setId: Long, newExerciseName: String) {
        viewModelScope.launch {
            trackerRepository.updateExerciseName(setId, newExerciseName)
            if (!df.existsInDefault(newExerciseName) &&
                !exerciseBankRepository.isExerciseNameInBank(newExerciseName)
            ) {
                val addToBankResult = exerciseBankRepository.addExerciseNameToBank(newExerciseName)
                if (addToBankResult is Result.Success) {
                    showSnackbarMessage(R.string.new_exercise_added_to_bankn)
                } else {
                    // notify of failure
                }
            }
        }
    }

    private fun getSetFromId(setId: Long): ExerciseSet? {
        val matches: List<ExerciseSet>? = exerciseSetsMediator.value?.filter { it.id == setId }
        if (matches != null && matches.isNotEmpty()) return matches[0]
        return null
    }

    fun onChangeRepRange(newLowerReps: Int, newHigherReps: Int) {
        val s = currentlySelectedExerciseSet.value
        if (s != null) {
            viewModelScope.launch {
                trackerRepository.updateRepRange(s.id, newLowerReps, newHigherReps)
            }
        }
    }

    fun onChangeRestTime(newRestTime: Int) {
        val s = currentlySelectedExerciseSet.value
        if (s != null) {
            viewModelScope.launch {
                trackerRepository.updateRestTime(s.id, newRestTime)
            }
        }
    }

    fun onDeleteSet() {
        val s = currentlySelectedExerciseSet.value
        if (s != null) {
            viewModelScope.launch {
                trackerRepository.deleteExerciseSet(s)
            }
        }
    }

    fun setSelectedExerciseNameText(exerciseName: String) {
        _selectedExerciseNameText.value = exerciseName
    }

    /**
     * numSets will be the amount of sets to add. Minimum is 1, max is 21.
     */
    fun onAddExerciseSets(
        position: Int, exerciseName: String,
        lowerReps: Int, higherReps: Int, restTime: Int, numSets: Int
    ) {
        val trainingData = args.value ?: return
        viewModelScope.launch {
            trackerRepository.addExerciseSets(
                position = position,
                numSets = numSets,
                exerciseName = exerciseName,
                lowerReps = lowerReps,
                higherReps = higherReps,
                restTime = restTime,
                cycleId = trainingData.first.id,
                workoutId = trainingData.second.id
            )
        }
    }

    fun onSetOptionsClicked(setNum: Int) {
        val exerciseSet = exerciseSetsMediator.value?.get(setNum)
        _currentlySelectedExerciseSet.value = exerciseSet
        _openBottomSheetEvent.value = Event(setNum)
    }

    /** Called by BottomSheetEditSets **/
    fun onChangeExerciseClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_EXERCISE)
    }

    fun onChangeRepClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_REP_RANGE)
    }

    fun onChangeRestTimeClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_REST_TIME)
    }

    fun onDeleteSetClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.DELETE_SET)
    }

    fun onInsertSetBeforeClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.INSERT_SET_BEFORE)
    }

    fun onInsertSetAfterClicked(setNum: Int) {
        _bottomSheetOptionSelectedEvent.value =
            Event(EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.INSERT_SET_AFTER)
    }

    fun onDismissBottomSheet() {
        _dismissBottomSheetEvent.value = Event(0)
    }

    // Util methods
    fun getColor(resID: Int) = ContextCompat.getColor(context, resID)

    fun getSetCount(): Int {
        return _exerciseSetsSource.value?.count() ?: 0
    }


    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}