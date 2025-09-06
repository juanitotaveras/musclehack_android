package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.TrainingPagerBinding
import com.musclehack.targetedHypertrophyTraining.setupSnackbar
import com.musclehack.targetedHypertrophyTraining.utilities.*
import com.musclehack.targetedHypertrophyTraining.workoutTracker.CustomExerciseAddedEvent
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ChangeSetExerciseDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.TrainingPagerData
import dagger.android.support.DaggerFragment
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Author: Juanito Taveras
 * Created: 11/25/17
 * Modified: 11/25/17 12/26/17
 */

class TrainingPagerFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }

    @Inject
    lateinit var pageTitleHelper: PageTitleHelper
    private lateinit var viewDataBinding: TrainingPagerBinding
    private var pausedTime: Int = 0
    private var killTimerBoxTask: KillTimerBoxTask? = null
    private val isLoading = AtomicBoolean(true)

    private val args: TrainingPagerFragmentArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.setupTrainingPagerEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { trainingData ->
                    setupPager(trainingData)
                }
            })

        viewModel.requestDefocusEvent.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            hideSoftKeyboard(viewDataBinding.container.rootView)
        })
        setupBottomSheet()
        setupFab()
        setupNavigation()
        setupTimer()
        setupToast()
        setupLoadingAnimation()
        setupSnackbar()
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupLoadingAnimation() {
        viewModel.showLoadingAnimationForList.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { shouldShowLoading ->
                if (shouldShowLoading) {
                    viewDataBinding.trainingPagerProgress.visibility = View.VISIBLE
                } else {
                    viewDataBinding.trainingPagerProgress.visibility = View.INVISIBLE
                }
            })
    }

    private fun setupTimer() {
        viewModel.nextExerciseNameLoadedEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { nextExerciseName ->
                    val logCard = viewModel.currentlySelectedLogCard.value ?: return@Observer
                    if (killTimerBoxTask != null) {
                        killTimerBoxTask?.cancel(true)
                        killTimerBoxTask = null
                    }
                    checkForPostNotificationsPermission()
                    requestTimerPermissionAndStart(
                        exerciseName = nextExerciseName,
                        restTime = logCard.exerciseSet.restTime
                    )
                }
            })
        viewModel.getTimerChangeEvent()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { event ->
                /** These events are coming from our timer service. */
                event.getContentIfNotHandled()?.let { timerChangeEvent ->
                    when (timerChangeEvent.type) {
                        TimerEventType.TICK -> {
                            timerTickUiUpdate(timerChangeEvent.newTime)
                        }

                        TimerEventType.STOP -> {
                            timerStopUiUpdate()
                        }

                        TimerEventType.FINISH -> {
                            timerFinishUiUpdate()
                        }

                        else -> {}
                    }
                }
            })
    }

    private fun setupBottomSheet() {
        viewModel.openBottomSheetEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                LogPageBottomSheetDialogFragment().show(
                    (context as AppCompatActivity)
                        .supportFragmentManager, "logPageBottomSheetFrag"
                )
            }
        }

        viewModel.bottomSheetOptionSelectedEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    val ctxt = context as? AppCompatActivity ?: return@Observer
                    val selectedLogCard =
                        viewModel.currentlySelectedLogCard.value ?: return@Observer
                    when (it) {
                        LogPageBottomSheetDialogFragment.LogPageBottomSheetOption.SEE_EXERCISE_TUTORIAL -> {
                            val link =
                                viewModel.getExerciseTutorialLink(selectedLogCard.getExerciseName())
                            if (link != null) {
                                BrowserUtils.openUrlInChromeCustomTabs(ctxt, link)
                            } else {
                                // notify that it wasn't found
                                // TODO: Notify the UI
                            }
                        }

                        LogPageBottomSheetDialogFragment.LogPageBottomSheetOption.SUBSTITUTE_EXERCISE -> {
                            ChooseSubstitutionTypeDialogFragment().show(
                                (context as AppCompatActivity).supportFragmentManager,
                                FRAG_CHOOSE_EX_SUB_TYPE
                            )
                        }

                        LogPageBottomSheetDialogFragment.LogPageBottomSheetOption.SKIP_EXERCISE -> {
                            viewModel.onSkipExercise()
                        }

                        LogPageBottomSheetDialogFragment.LogPageBottomSheetOption.UNDO_SKIP_EXERCISE -> {
                            viewModel.onUndoSkipExercise()
                        }
                    }
                    viewModel.onDismissBottomSheet()
                }
            })
        setTitle()
    }

    private fun setupNavigation() {
        viewModel.editNoteEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { setId ->
                val editLogNoteDialogFragment = EditLogNoteDialogFragment()
                activity?.supportFragmentManager?.let { mgr ->
                    editLogNoteDialogFragment.show(mgr, FRAG_EDIT_LOG_NOTE)
                }
            }
        }
        viewModel.openPrevNoteEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { setId ->
                val prevLogNoteDialogFragment = PreviousLogNoteDialogFragment()
                activity?.supportFragmentManager?.let { mgr ->
                    prevLogNoteDialogFragment.show(mgr, FRAG_PREV_LOG_NOTE)
                }
            }
        }
        viewModel.exerciseSubTypeSelectedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { type ->
                when (type) {
                    ExerciseSubstitutionType.SINGLE_DAY -> {
                        ChangeLogExerciseDialogFragment().show(
                            (context as AppCompatActivity).supportFragmentManager,
                            FRAG_CHANGE_LOG_EXERCISE
                        )
                    }

                    ExerciseSubstitutionType.ENTIRE_CYCLE -> {
                        val logCardModel = viewModel.currentlySelectedLogCard.value
                        val frag = if (logCardModel == null) {
                            ChangeSetExerciseDialogFragment()
                        } else {
                            ChangeSetExerciseDialogFragment.newInstance(
                                logCardModel.exerciseSet.id,
                                logCardModel.exerciseSet.exerciseName
                            )
                        }
                        frag.show(
                            (context as AppCompatActivity).supportFragmentManager,
                            FRAG_CHANGE_SET_EXERCISE
                        )
                    }
                }
            }
        }
    }

    private fun setupToast() {
        viewModel.notifyCycleCompletedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val toast = Toast.makeText(
                    activity,
                    R.string.cycle_completion_message,
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.BOTTOM, 0, 150)
                toast.show()
            }
        }
    }

    private fun setTitle() {
        viewModel.args.observe(viewLifecycleOwner) { data ->
            (activity as? AppCompatActivity)?.let { act ->
                act.supportActionBar?.title = data.second.name
            }
        }
    }

    private fun setupFab() {
        (activity as? MainActivity)?.clearFab()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = TrainingPagerBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        viewDataBinding.stopIcon.setOnClickListener {
            stopTimerService()
        }
        viewDataBinding.pauseIcon.setOnClickListener {
            /* Change pause icon to a play icon */
            //        pauseButton.
            viewDataBinding.pauseIcon.visibility = View.GONE
            viewDataBinding.playIcon.visibility = View.VISIBLE
            /* Save paused time. */
            val i = Intent(activity, TimerService::class.java)
            i.putExtra(KEY_TIMER_EVENT_TYPE, TimerCommandType.PAUSE.toString())
            ContextCompat.startForegroundService(requireContext(), i)
        }
        viewDataBinding.playIcon.setOnClickListener {
            /* Change pause icon to a play icon */
            //        pauseButton.
            viewDataBinding.playIcon.visibility = View.GONE
            viewDataBinding.pauseIcon.visibility = View.VISIBLE
            resumeTimerService()
        }

        viewModel.start(args.cycleId, args.workoutId)

        return viewDataBinding.root
    }

    /**
     * Requests exact alarm permission if needed, then starts the timer service.
     * This ensures compliance with Google Play's Exact Alarm Permission policy.
     */
    private fun requestTimerPermissionAndStart(restTime: Int, exerciseName: String) {
        val alarmPermissionHelper = AlarmPermissionHelper.create(requireContext())

        alarmPermissionHelper.ensureExactAlarmPermission(
            onPermissionGranted = {
                startTimerService(restTime, exerciseName)
            },
            onPermissionDenied = {
                // Still start the timer service - it will use regular alarms as fallback
                startTimerService(restTime, exerciseName)
            }
        )
    }

    private fun startTimerService(restTime: Int, exerciseName: String) {
        // TODO: Consider removing these UI changes.
        // UI changes should come directly from TimerService.
        viewDataBinding.timerBox.visibility = View.VISIBLE
        viewDataBinding.overlayRestTimeText.text = TimerService.secondsToMinutesFormat(restTime)
        val i = Intent(activity, TimerService::class.java)
        i.putExtra(KEY_REST_TIME, restTime)
        i.putExtra(KEY_CLICK_TIME, Date().time)
        i.putExtra(KEY_UPCOMING_EXERCISE, exerciseName)
        i.putExtra(KEY_TIMER_EVENT_TYPE, TimerCommandType.START.toString())
        ContextCompat.startForegroundService(requireContext(), i)
    }

    private fun resumeTimerService() {
        val i = Intent(activity, TimerService::class.java)
        i.putExtra(KEY_CLICK_TIME, Date().time)
        i.putExtra(KEY_TIMER_EVENT_TYPE, TimerCommandType.RESUME.toString())
        ContextCompat.startForegroundService(requireContext(), i)
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    private fun stopTimerService() {
        val i = Intent(activity, TimerService::class.java)
        i.putExtra(KEY_TIMER_EVENT_TYPE, TimerCommandType.STOP.toString())
        ContextCompat.startForegroundService(requireContext(), i)
    }

    private fun timerTickUiUpdate(newTime: Int) {
        if (viewDataBinding.timerBox.visibility == View.INVISIBLE) {
            viewDataBinding.timerBox.visibility = View.VISIBLE
        }
        /* Maybe only save this if our boolean says service has been stopped. */
        if (newTime > 0) {
            pausedTime = newTime
            viewDataBinding.overlayRestTimeText.text = TimerService.secondsToMinutesFormat(newTime)
        }
    }

    private fun timerStopUiUpdate() {
        if (viewDataBinding.pauseIcon.visibility == View.GONE) {
            viewDataBinding.playIcon.visibility = View.GONE
            viewDataBinding.pauseIcon.visibility = View.VISIBLE
        }
        viewDataBinding.timerBox.visibility = View.INVISIBLE
    }

    /** Called only when our timer finished without being stopped manually. */
    private fun timerFinishUiUpdate() {
        killTimerBoxTask = KillTimerBoxTask(view)
        killTimerBoxTask?.execute()
    }

    /* Show 0 on rest timer for 2 seconds before making it disappear. */
    class KillTimerBoxTask(v: View?) : AsyncTask<Void, Void, Void?>() {
        private val viewRef: WeakReference<View>? = if (v != null) WeakReference(v) else null
        override fun onPreExecute() {
            val restTimeText = viewRef?.get()?.findViewById<TextView>(R.id.overlayRestTimeText)
            restTimeText?.text = TimerService.secondsToMinutesFormat(0)
        }

        override fun doInBackground(vararg unused: Void): Void? {
            if (!isCancelled)
                SystemClock.sleep(2000)
            return null
        }

        override fun onPostExecute(unused: Void?) {
            val timerBoxRef = viewRef?.get()?.findViewById<LinearLayout>(R.id.timerBox)
            timerBoxRef?.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        /** Hides the timer box, for the event in which the timer runs out when app
         * is in the background. */
        viewDataBinding.timerBox.visibility = View.INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_training_page, menu)
    }

    private fun setupPager(data: TrainingPagerData) {
        val adapter = TrainingPagerAdapter(activity as FragmentActivity, viewModel)
        viewDataBinding.trainingPager.adapter = adapter
        TabLayoutMediator(viewDataBinding.tabs, viewDataBinding.trainingPager) { tab, position ->
            val frequency = viewModel.getArgs()?.second?.repeats
            val workoutName = viewModel.getArgs()?.second?.name
            if (frequency != null && workoutName != null) {
                tab.text = pageTitleHelper.getPageTitleForTab(position, frequency, workoutName)
            }
        }.attach()
        viewDataBinding.trainingPager.isSaveEnabled = false
        viewModel.scrollToLastViewedPageEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { position ->
                    viewDataBinding.trainingPager.setCurrentItem(position, false)
                    viewModel.hasScrolledToStartPosition()
                }
            })
        viewDataBinding.trainingPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // tell viewmodel we are changing day
                viewModel.onTrainingPageSelected(position)
                super.onPageSelected(position)
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_sets -> {
                val action =
                    TrainingPagerFragmentDirections.actionTrainingPagerFragmentToEditSetsFragment(
                        args.cycleId,
                        args.workoutId
                    )
                findNavController().navigate(action)
                return true
            }

            R.id.skipDay -> {
                if (viewModel.shouldShowSkipDayDialog()) {
                    ConfirmSkipDayDialogFragment().show(
                        (context as AppCompatActivity).supportFragmentManager, FRAG_CONFIRM_SKIP_DAY
                    )
                } else {
                    viewModel.onSkipDay()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun onCustomExerciseAdded(event: CustomExerciseAddedEvent) {
        val s = getString(R.string.custom_exercise_added_toast)
        val toast = Toast.makeText(
            activity, String.format(s, event.exerciseName),
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 200)
        toast.show()
    }

    private fun checkForPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val activity: MainActivity = activity as? MainActivity ?: return
        val hasPostNotificationPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPostNotificationPermission) {
            activity.launchNotificationPermissionRequest()
        }
    }
}
