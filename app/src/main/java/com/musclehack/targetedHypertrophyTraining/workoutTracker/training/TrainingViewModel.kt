package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.StringRes
import com.musclehack.targetedHypertrophyTraining.R
import androidx.lifecycle.*
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.ExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.exerciseBank.DefaultExercises
import com.musclehack.targetedHypertrophyTraining.notifyObserver
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOW_SKIP_DAY_WARNING
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseLog
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseNote
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.TrainingPagerData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Created by juanito on 12/23/2017.
 * Modified 3/10/18
 */

class TrainingViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val exerciseBankRepository: ExerciseBankRepository,
    private val sharedPreferences: SharedPreferences
) :
    ViewModel() {
    @Inject
    lateinit var appContext: Context
    private var scrollPosition = AtomicReference(0)
    var currentDay: AtomicReference<Int?> = AtomicReference(null)

    val args: MutableLiveData<TrainingPagerData> = MutableLiveData()

    private val _exerciseSetsSource: LiveData<List<ExerciseSet>> = args.switchMap { tData ->
        if (tData != null)
            trackerRepository.getExerciseSets(tData.first.id, tData.second.id)
        else MutableLiveData()
    }

    private val _logCardModelsMediator = MediatorLiveData<List<LogCardModel>>()
    val logCardModels: LiveData<List<LogCardModel>> = _logCardModelsMediator

    private var hasScrolledToStartPosition = false

    /** Used for keeping track of where the user is editing text */
    private val currentlyFocusedExerciseSet = MutableLiveData<Int?>(null)

    /** Used for determining an action we performed on a set (such as editing the note). */
    private val currentSelectedExerciseSet = MutableLiveData<ExerciseSet?>(null)
    private val _currentlyEditingNote = MutableLiveData<ExerciseNote>(null)
    val currentlyEditingNote: LiveData<ExerciseNote> = _currentlyEditingNote

    /** Used by our bottom sheet */
    private val _currentlySelectedLogCard = MutableLiveData<LogCardModel?>(null)
    val currentlySelectedLogCard: LiveData<LogCardModel?> = _currentlySelectedLogCard

    private val df by lazy {
        DefaultExercises(appContext.resources, appContext.packageName)
    }

    private val _dismissBottomSheetEvent = MutableLiveData<Event<Int>>()
    val dismissBottomSheetEvent: LiveData<Event<Int>> = _dismissBottomSheetEvent

    /**
     * Events
     */

    private val _setupTrainingPagerEvent = MutableLiveData<Event<TrainingPagerData>>()
    val setupTrainingPagerEvent: LiveData<Event<TrainingPagerData>> = _setupTrainingPagerEvent

    private val _scrollToLastViewedPageEvent = MutableLiveData<Event<Int>>()
    val scrollToLastViewedPageEvent: LiveData<Event<Int>> = _scrollToLastViewedPageEvent

    private val _showLoadingAnimationForList = MutableLiveData(true)
    val showLoadingAnimationForList: LiveData<Boolean> = _showLoadingAnimationForList

    private val _requestDefocusEvent = MutableLiveData<Event<Int>>()
    val requestDefocusEvent: LiveData<Event<Int>> = _requestDefocusEvent

    private val _notifySetNumChangedEvent = MutableLiveData<Event<Int>>()
    val notifySetNumChangedEvent: LiveData<Event<Int>> = _notifySetNumChangedEvent

    // Argument is the position of the exerciseSet
    private val _nextExerciseNameLoadedEvent = MutableLiveData<Event<String>>()
    val nextExerciseNameLoadedEvent: LiveData<Event<String>> = _nextExerciseNameLoadedEvent

    private val _editNoteEvent = MutableLiveData<Event<Int>>()
    val editNoteEvent: LiveData<Event<Int>> = _editNoteEvent

    private val _openPrevNoteEvent = MutableLiveData<Event<Int>>()
    val openPrevNoteEvent: LiveData<Event<Int>> = _openPrevNoteEvent

    private val _openBottomSheetEvent = MutableLiveData<Event<Int>>()
    val openBottomSheetEvent: LiveData<Event<Int>> = _openBottomSheetEvent

    private val _exerciseSubTypeSelectedEvent = MutableLiveData<Event<ExerciseSubstitutionType>>()
    val exerciseSubTypeSelectedEvent: LiveData<Event<ExerciseSubstitutionType>> =
        _exerciseSubTypeSelectedEvent

    private val _notifyCycleCompletedEvent = MutableLiveData<Event<Boolean>>()
    val notifyCycleCompletedEvent: LiveData<Event<Boolean>> = _notifyCycleCompletedEvent

    private val _bottomSheetOptionSelectedEvent =
        MutableLiveData<Event<LogPageBottomSheetDialogFragment.LogPageBottomSheetOption>>()
    val bottomSheetOptionSelectedEvent:
            LiveData<Event<LogPageBottomSheetDialogFragment.LogPageBottomSheetOption>> =
        _bottomSheetOptionSelectedEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    /**
     * Called immediately after ViewModel is created.
     * Would be nice for this to be a constructor.
     */
    fun start(cycleId: Long?, workoutId: Long?) {
        _showLoadingAnimationForList.value = true // show loading animation
        if (args.value == null || args.value!!.second.id != workoutId) {
            viewModelScope.launch {
                val trainingData = getCycleAndWorkoutData(cycleId, workoutId)
                if (trainingData != null) {
                    currentDay.set(trainingData.second.lastDayViewed)
                    args.value = trainingData
                    _scrollToLastViewedPageEvent.value = Event(currentDay.get()!!)
                    _setupTrainingPagerEvent.value = Event(trainingData)
                } else {
                    // log erre
                    Log.e("x", "ERROR!")
                }
            }
        } else if (args.value != null) {
            /** We do not need to reload data because workoutID is the same. */
            _setupTrainingPagerEvent.value = Event(args.value!!)
            _scrollToLastViewedPageEvent.value = Event(currentDay.get()!!)
            _showLoadingAnimationForList.value = false
        }
    }

    /** Should only be called when user manually chooses a day. */
    fun onTrainingPageSelected(day: Int) {
        if (hasScrolledToStartPosition) {
            currentDay.set(day)
            saveSelectedDay()
            triggerRefreshOfExerciseLogs()
        }
    }

    fun saveSelectedDay() {
        args.value?.second?.let { workout ->
            viewModelScope.launch {
                workout.lastDayViewed = currentDay.get()!!
                trackerRepository.updateWorkout(workout)
            }
        }
    }

    fun hasScrolledToStartPosition() {
        hasScrolledToStartPosition = true
        Log.e("p", "has scrolled to start")
    }

    /** Saved our 'date last trained' for this workout */
    fun saveTrainingTime() {
        args.value?.first?.let { cycle ->
            viewModelScope.launch {
                trackerRepository.saveTrainingDate(cycle.id)
            }
        }
    }

    /** Returns the arguments used to initialize the view model (workoutID, cycleID, etc).*/
    fun getArgs(): TrainingPagerData? {
        return args.value
    }

    /**
     * Database calls
     */
    private suspend fun getCycleAndWorkoutData(
        cycleId: Long?,
        workoutId: Long?
    ): TrainingPagerData? {
        if (cycleId != null && workoutId != null) {
            val result: Result<TrainingPagerData> = trackerRepository
                .getTrainingPagerData(cycleId, workoutId)
            if (result is Result.Success) {
                return result.data
            }
        }
        // log error to UI here
        return null
    }

    fun onNoteSaved(newNote: String) {
        viewModelScope.launch {
            val exerciseSet: ExerciseSet? = currentSelectedExerciseSet.value
            if (exerciseSet == null || currentDay.get() == null) return@launch
            trackerRepository.insertNote(exerciseSet.id, currentDay.get()!!, newNote)
            val logCard = getLogCard(exerciseSet.position)
            logCard?.exerciseLog?.hasNote = newNote.isNotEmpty()
            _notifySetNumChangedEvent.value = Event(exerciseSet.position)

            _currentlyEditingNote.value = null
            currentSelectedExerciseSet.value = null
        }
    }

    private fun triggerRefreshOfExerciseLogs() {
        args.notifyObserver()
    }

    fun getLogCard(setNum: Int): LogCardModel? {
        return logCardModels.value?.get(setNum)
    }

    fun getSetCount(): Int = logCardModels.value?.count() ?: 0

    private var coroutine: Job? = null

    init {
        _logCardModelsMediator.addSource(_exerciseSetsSource) { eSets ->
            if (coroutine != null && !coroutine!!.isCompleted) {
                // TODO: I don't thnk this is needed. Consider deleting.
                coroutine?.cancel(CancellationException("XYZ"))
            }
            coroutine = viewModelScope.launch {
                currentDay.get()?.let { day ->
                    val logCardModels = trackerRepository.getLogCardModels(eSets, day)
                    _logCardModelsMediator.value = logCardModels
                    _showLoadingAnimationForList.value = false
                }
            }
        }
    }

    private fun getSet(setNum: Int): ExerciseSet? {
        return logCardModels.value?.get(setNum)?.exerciseSet
    }

    fun getPageCount(): Int {
        val v = args.value
        return if (v != null) {
            if (v.second.repeats > 1) v.first.numWeeks * v.second.repeats else v.first.numWeeks
        } else {
            0
        }
    }

    /**
     * Called directly by UI
     */
    fun onEditTextFocused(hasFocus: Boolean, setNum: Int) {
        if (hasFocus) {
            currentlyFocusedExerciseSet.value = setNum
            logCardModels.value?.get(setNum)?.isFocused = true // used for RecyclerView
        } else {
            currentlyFocusedExerciseSet.value = null
            logCardModels.value?.get(setNum)?.isFocused = false // used for RecyclerView
        }
    }

    fun onAddNoteClicked(setNum: Int) {
        viewModelScope.launch {
            val setId = getSet(setNum)?.id
            val day = currentDay.get()
            if (setId != null && day != null) {
                val exerciseNoteResult = trackerRepository.getNote(setId, day)
                if (exerciseNoteResult is Result.Success) {
                    _currentlyEditingNote.value = exerciseNoteResult.data
                    // trigger event so fragment can show dialog
                    _editNoteEvent.value = Event(setNum)
                    currentSelectedExerciseSet.value = getSet(setNum)
                } else {
                    // Note has never been created.
                    _currentlyEditingNote.value = null
                    _editNoteEvent.value = Event(setNum)
                    currentSelectedExerciseSet.value = getSet(setNum)
                }
            }
        }
    }

    fun onPrevNoteClicked(setNum: Int, prevNoteDay: Int?) {
        if (prevNoteDay == null) return
        viewModelScope.launch {
            val setId = getSet(setNum)?.id
            if (setId != null) {
                val exerciseNoteResult = trackerRepository.getNote(setId, prevNoteDay)
                // the note will be null if it's never been created.
                if (exerciseNoteResult is Result.Success) {
                    _currentlyEditingNote.value = exerciseNoteResult.data
                    // trigger event so fragment can show dialog
                    _openPrevNoteEvent.value = Event(setNum)
                    currentSelectedExerciseSet.value = getSet(setNum)
                } else {
                    // UI error
                }
            }
        }
    }

    fun getExerciseTutorialLink(exerciseName: String): String? {
        return df.getTutorialLink(exerciseName)
    }

    fun onWeightTextChanged(txt: String, setNum: Int, day: Int) {
        if (day != currentDay.get()) {
            return
        }
        // TODO: Round down
        val newWeight: Double?
        if (txt.isNotEmpty() && txt != ".") {
            newWeight = txt.toDouble()
        } else {
            newWeight = null
        }

        val logCardModel: LogCardModel = logCardModels.value?.get(setNum) ?: return
        if (newWeight == logCardModel.exerciseLog.weight) {
            return
        }
        logCardModel.exerciseLog.weight = newWeight
        viewModelScope.launch {
            val setId = logCardModel.exerciseSet.id
            trackerRepository.updateWeight(setId, day, newWeight)
        }
    }

    fun onRepsTextChanged(repsStr: String, setNum: Int, day: Int) {
        if (day != currentDay.get()) {
            return
        }
        val newReps: Int?
        // TODO: Handle number format exception
        if (repsStr.isNotEmpty() && repsStr != ".") {
            newReps = repsStr.toInt()
        } else {
            newReps = null
        }
        val logCardModel: LogCardModel = logCardModels.value?.get(setNum) ?: return
        if (newReps == logCardModel.exerciseLog.reps) {
            return
        }
        logCardModel.exerciseLog.reps = newReps
        viewModelScope.launch {
            trackerRepository.updateReps(
                logCardModel.exerciseLog.setId,
                logCardModel.exerciseLog.day,
                newReps
            )
        }
    }

    fun onSkipDay() {
        val args = args.value ?: return
        val day = currentDay.get() ?: return
        viewModelScope.launch {
            trackerRepository.skipDay(args.first.id, args.second.id, day)
            triggerRefreshOfExerciseLogs()
        }
    }

    fun onChangeLogExerciseName(newExerciseName: String) {
        val logCard = currentlySelectedLogCard.value ?: return
        viewModelScope.launch {
            val setId = logCard.exerciseSet.id
            val day = logCard.exerciseLog.day
            val result: Result<ExerciseLog?> =
                trackerRepository.changeLogExerciseName(setId, day, newExerciseName)
            if (result is Result.Success) {
                val newLog = result.data
                // TODO: Replace our logCard instead
                logCard.exerciseLog.subName = newLog?.subName
                _notifySetNumChangedEvent.postValue(Event(logCard.exerciseSet.position))
                if (!df.existsInDefault(newExerciseName) &&
                    !exerciseBankRepository.isExerciseNameInBank(newExerciseName)
                ) {
                    val addToBankResult =
                        exerciseBankRepository.addExerciseNameToBank(newExerciseName)
                    if (addToBankResult is Result.Success) {
                        showSnackbarMessage(R.string.new_exercise_added_to_bankn)
                    } else {
                        // notify of failure
                    }
                }

            } else {
                showSnackbarMessage(R.string.failed_to_substitute_exercise)
            }
        }
    }

    fun onExerciseSubTypeSelected(type: ExerciseSubstitutionType) {
        _exerciseSubTypeSelectedEvent.value = Event(type)
    }

    fun setScrollPosition(scrollPosition: Int, day: Int) {
        if (currentDay.get() != null && currentDay.get() == day) {
            this.scrollPosition.set(scrollPosition)
        }
    }

    fun getScrollPosition(): Int {
        return if (scrollPosition.get() == null) 0
        else scrollPosition.get()
    }

    /** This method is called when we tap the "start timer" button on an Exercise Set. */
    fun onStartTimerClicked(setNum: Int) {
        _currentlySelectedLogCard.value = getLogCard(setNum)
        viewModelScope.launch {
            val exSet = getSet(setNum)
            val day = currentDay.get()
            if (exSet != null && day != null) {
                val nameResult = trackerRepository.getNextExerciseName(exSet.id, day)
                if (nameResult is Result.Success) {
                    _nextExerciseNameLoadedEvent.value = Event(nameResult.data)
                } else {
                    Log.e("class", "Could not get next exercise name.")
                    _nextExerciseNameLoadedEvent.value = Event("")
                }
            }
        }
    }

    /** Called when the user presses the 'done' button. */
    fun notifyCycleCompleted() {
        _notifyCycleCompletedEvent.value = Event(true)
    }

    fun onLogCardOptionsClicked(setNum: Int) {
        _currentlySelectedLogCard.value = getLogCard(setNum)
        _openBottomSheetEvent.value = Event(setNum)
    }

    fun shouldShowSkipDayDialog(): Boolean {
        return sharedPreferences.getBoolean(PREF_SHOW_SKIP_DAY_WARNING, true)
    }

    fun setShouldNotShowSkipDayDialog() {
        sharedPreferences.edit().putBoolean(PREF_SHOW_SKIP_DAY_WARNING, false).apply()
    }

    fun onSkipExercise() {
        val logCard = currentlySelectedLogCard.value ?: return
        logCard.exerciseLog.skip = true
        viewModelScope.launch {
            trackerRepository.skipExercise(logCard.exerciseSet.id, logCard.exerciseLog.day)
            _notifySetNumChangedEvent.value = Event(logCard.exerciseSet.position)
        }
    }

    fun onUndoSkipExercise() {
        val logCard = currentlySelectedLogCard.value ?: return
        logCard.exerciseLog.skip = false
        viewModelScope.launch {
            trackerRepository.undoSkipExercise(logCard.exerciseSet.id, logCard.exerciseLog.day)
            _notifySetNumChangedEvent.value = Event(logCard.exerciseSet.position)
        }
    }

    fun getTimerChangeEvent(): LiveData<Event<TimerChangeEvent>> {
        return trackerRepository.observeTimerChangeEvent()
    }

    /**
     * Called when we want to un-focus a focused EditText.
     */
    fun onRequestDefocus() {
        currentlyFocusedExerciseSet.value?.let {
            _requestDefocusEvent.value = Event(it)
        }
    }

    /**
     * Called by BottomSheet Data Binding.
     */
    fun onSeeExerciseTutorialSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(
                LogPageBottomSheetDialogFragment
                    .LogPageBottomSheetOption.SEE_EXERCISE_TUTORIAL
            )
    }

    fun onSubstituteExerciseSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(
                LogPageBottomSheetDialogFragment
                    .LogPageBottomSheetOption.SUBSTITUTE_EXERCISE
            )
    }

    fun onSkipExerciseSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(
                LogPageBottomSheetDialogFragment
                    .LogPageBottomSheetOption.SKIP_EXERCISE
            )
    }

    fun onUndoSkipExerciseSelected() {
        _bottomSheetOptionSelectedEvent.value =
            Event(
                LogPageBottomSheetDialogFragment
                    .LogPageBottomSheetOption.UNDO_SKIP_EXERCISE
            )
    }

    /**
     * Dismiss bottom sheet.
     */
    fun onDismissBottomSheet() {
        _dismissBottomSheetEvent.value = Event(0)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
