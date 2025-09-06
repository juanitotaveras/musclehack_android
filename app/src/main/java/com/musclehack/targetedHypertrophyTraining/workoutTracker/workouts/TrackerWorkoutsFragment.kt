package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

//import com.musclehack.targetedHypertrophyTraining.DatabaseHelper
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.musclehack.targetedHypertrophyTraining.BottomNavigationActivity
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentTrackerWorkoutsBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Author: Juanito Taveras
 * Created: 11/24/17
 * Modified: 11/24/17 11/25/17 1/25/18
 */

class TrackerWorkoutsFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackerWorkoutsViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentTrackerWorkoutsBinding
    private lateinit var listAdapter: WorkoutsAdapter2
    private var dataObserver: RecyclerView.AdapterDataObserver? = null
    private lateinit var layoutManager: LinearLayoutManager

    private val args: TrackerWorkoutsFragmentArgs by navArgs()

    private var contextMenu: Menu? = null
    private var helperText: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentTrackerWorkoutsBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        viewModel.start(args.cycleId)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupListAdapter()
        setupNavigation()
        setupBottomSheet()
        setTitleBar()
        setupFab()
        (activity as? BottomNavigationActivity)?.forceBottomNavigationToAppear()
        setHasOptionsMenu(true)
    }

    private fun setTitleBar() {
        viewModel.cycle.observe(this.viewLifecycleOwner, androidx.lifecycle.Observer {
            (activity as AppCompatActivity).let { act ->
                act.supportActionBar?.title = it?.name
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        contextMenu = menu
        contextMenu?.clear()
        inflater.inflate(R.menu.workouts_drawer, contextMenu)
    }

    private fun setupNavigation() {
        viewModel.openWorkoutEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { workoutId ->
                    viewModel.cycle.value?.id?.let { cycleId ->
                        openWorkout(cycleId, workoutId)
                    }
                }
            })
        viewModel.trackerWorkoutsSortMode.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { isOn ->
                val activity = activity as? AppCompatActivity
                val menu = contextMenu
                if (activity != null && menu != null) {
                    if (isOn) {
                        // change context menu
                        activity.supportActionBar?.title = getString(R.string.sort_workouts_title)
                        menu.clear()
                        activity.menuInflater.inflate(R.menu.menu_sort_mode, menu)
                    } else {
                        activity.supportActionBar?.title = viewModel.cycle.value?.name
                        menu.clear()
                        activity.menuInflater.inflate(R.menu.workouts_drawer, menu)
                    }
                }
            })
        (activity as? MainActivity)?.let { activity ->
            activity.backPressedEvent.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { event ->
                    // cancel sortMode if user presses 'back'
                    event.getContentIfNotHandled()?.let {
                        if (viewModel.trackerWorkoutsSortMode.value == true) {
                            viewModel.cancelSortMode()
                        }
                    }
                })
        }
    }

    private fun setupBottomSheet() {
        viewModel.openBottomSheetEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    TrackerWorkoutsBottomSheetDialogFragment().show(
                        (context as AppCompatActivity)
                            .supportFragmentManager, "trackerWorkoutsBottomSheetFrag"
                    )
                }
            })
        viewModel.bottomSheetOptionSelectedEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    when (it) {
                        TrackerWorkoutsBottomSheetDialogFragment.TrackerWorkoutsBottomSheetOption.RENAME -> {
                            RenameWorkoutDialogFragment().show(
                                (context as AppCompatActivity)
                                    .supportFragmentManager, "frag_rename_workout"
                            )
                        }

                        TrackerWorkoutsBottomSheetDialogFragment.TrackerWorkoutsBottomSheetOption.DELETE -> {
                            ConfirmDeleteWorkoutDialogFragment().show(
                                (context as AppCompatActivity)
                                    .supportFragmentManager, "frag_confirm_delete_workout"
                            )
                        }
                    }
                    viewModel.onDismissBottomSheet()
                }
            })
    }

    private fun openWorkout(cycleId: Long, workoutId: Long) {
        val action = TrackerWorkoutsFragmentDirections
            .actionTrackerWorkoutsFragmentDestToTrainingPagerFragment(
                cycleId = cycleId,
                workoutId = workoutId
            )
        findNavController().navigate(action)
    }

    private fun setupFab() {
        (activity as? MainActivity)?.let { act ->
            act.findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
            act.fabClickedEvent.observe(viewLifecycleOwner, androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    val action = TrackerWorkoutsFragmentDirections
                        .actionTrackerWorkoutsFragmentDestToCreateNewWorkoutFragment()
                    findNavController().navigate(action)
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sortWorkoutsIcon -> {
                viewModel.turnOnSortMode()
                listAdapter.notifyDataSetChanged()
            }

            R.id.confirmSort -> {
                viewModel.confirmNewOrder()
                listAdapter.notifyDataSetChanged()
            }

            R.id.cancelSort -> {
                viewModel.cancelSortMode()
                listAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = WorkoutsAdapter2(viewModel = viewModel)
            viewDataBinding.trackerWorkoutsList.adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            viewDataBinding.trackerWorkoutsList.addItemDecoration(divider)
            viewModel.positionPlusOneClickedEvent.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    viewModel.justMovedPosition = it.peekContent()
                    listAdapter.notifyDataSetChanged()
                })
            viewModel.positionMinusOneClickedEvent.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    viewModel.justMovedPosition = it.peekContent()
                    listAdapter.notifyDataSetChanged()
                })
        } else {
            // log here
        }
    }

}
