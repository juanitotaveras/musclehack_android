package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentEditSetsBinding
import com.musclehack.targetedHypertrophyTraining.setupSnackbar
import com.musclehack.targetedHypertrophyTraining.showSnackbar
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CHANGE_REP_RANGE
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CHANGE_REST_TIME
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CHANGE_SET_EXERCISE
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CONFIRM_DELETE_SET
import com.musclehack.targetedHypertrophyTraining.workoutTracker.CustomExerciseAddedEvent
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class EditSetsFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }
    private lateinit var adapter: EditSetsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var contextMenu: Menu? = null
    private lateinit var viewDataBinding: FragmentEditSetsBinding
    private var dataObserver: RecyclerView.AdapterDataObserver? = null

    private val args: EditSetsFragmentArgs by navArgs()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        contextMenu = menu
        contextMenu?.clear()
        inflater.inflate(R.menu.menu_edit_sets, contextMenu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentEditSetsBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        viewModel.start(args.cycleId, args.workoutId)
        return viewDataBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        contextMenu = menu
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListAdapter()
        viewModel.modelLoadedEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                (activity as? AppCompatActivity)?.supportActionBar?.let {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setDisplayUseLogoEnabled(false)
                    val s = getString(R.string.edit_sets_title)
                    viewModel.args.value?.second?.name?.let { workoutName ->
                        it.title = String.format(s, workoutName)
                    }
                }
            }
        })
        viewModel.editSetsSortMode.observe(viewLifecycleOwner, Observer { isOn ->
            val activity = activity as? AppCompatActivity
            val menu = contextMenu
            if (activity != null && menu != null) {
                if (isOn) {
                    // change context menu
                    /* Replace action bar items with a cancel and OK button. */
                    activity.supportActionBar?.title = getString(R.string.sort_sets_mode_header)
                    menu.clear()
                    activity.menuInflater.inflate(R.menu.menu_sort_mode, menu)

                } else {
                    val s = getString(R.string.edit_sets_title)
                    viewModel.args.value?.second?.name?.let { workoutName ->
                        activity.supportActionBar?.title = String.format(s, workoutName)
                    }
                    menu.clear()
                    activity.menuInflater.inflate(R.menu.menu_edit_sets, menu)
                }
            }
        })
        setupBottomSheet()
        setupFab()
        setupNavigation()
        setupSnackbar()
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        (activity as? MainActivity)?.let { activity ->
            activity.backPressedEvent.observe(viewLifecycleOwner, Observer { event ->
                // cancel sortMode if user presses 'back'
                event.getContentIfNotHandled()?.let {
                    if (viewModel.editSetsSortMode.value == true) {
                        viewModel.cancelSortMode()
                    }
                }

            })
        }
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            adapter = EditSetsAdapter(viewModel = viewModel)
            viewDataBinding.editSetsList.adapter = adapter
            layoutManager = LinearLayoutManager(context)
            viewDataBinding.editSetsList.layoutManager = layoutManager
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            viewDataBinding.editSetsList.addItemDecoration(divider)
            dataObserver = object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                    val justMovedPos = viewModel.justMovedPosition
                    val plusOneIdx =
                        viewModel.positionPlusOneClickedEvent.value?.getContentIfNotHandled()
                    if (plusOneIdx != null) {
                        layoutManager.scrollToPosition(plusOneIdx + 1)
                        // Notify item changed so we reset sort buttons
                        viewModel.justMovedPosition = plusOneIdx + 1
                        adapter.notifyItemChanged(plusOneIdx)
                        adapter.notifyItemChanged(plusOneIdx + 1)
                    } else {
                        val minusOneIdx =
                            viewModel.positionMinusOneClickedEvent.value?.getContentIfNotHandled()
                        if (minusOneIdx != null) {
                            layoutManager.scrollToPosition(minusOneIdx - 1)
                            // Notify item changed so we reset sort buttons

                            viewModel.justMovedPosition = minusOneIdx - 1
                            adapter.notifyItemChanged(minusOneIdx)
                            adapter.notifyItemChanged(minusOneIdx - 1)
                        }
                    }
                    if (justMovedPos != null) {
                        // Resets previous grayed out item, if there was one.
                        adapter.notifyItemChanged(justMovedPos)
                    }
                }
            }
            dataObserver?.let { adapter.registerAdapterDataObserver(it) }
        } else {
            // log here!
        }
    }

    private fun setupBottomSheet() {
        viewModel.openBottomSheetEvent.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it.getContentIfNotHandled()?.let {
                EditSetsBottomSheetDialogFragment().show(
                    (context as AppCompatActivity)
                        .supportFragmentManager, "editSetsBottomSheetFrag"
                )
            }
        })
        viewModel.bottomSheetOptionSelectedEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    when (it) {
                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_EXERCISE -> {
                            /* Send SetID and current exercise */
                            val set = viewModel.currentlySelectedExerciseSet.value
                            if (set != null) {
                                val changeSetExerciseFrag =
                                    ChangeSetExerciseDialogFragment.newInstance(
                                        setID = set.id, exerciseName = set.exerciseName
                                    )
                                changeSetExerciseFrag.show(
                                    (context as AppCompatActivity).supportFragmentManager,
                                    FRAG_CHANGE_SET_EXERCISE
                                )
                            }
                        }

                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_REP_RANGE -> {
                            ChangeRepRangeDialogFragment().show(
                                (context as AppCompatActivity)
                                    .supportFragmentManager,
                                FRAG_CHANGE_REP_RANGE
                            )
                        }

                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.CHANGE_REST_TIME -> {
                            ChangeRestTimeDialogFragment().show(
                                (context as AppCompatActivity).supportFragmentManager,
                                FRAG_CHANGE_REST_TIME
                            )
                        }

                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.DELETE_SET -> {
                            ConfirmDeleteSetDialogFragment().show(
                                (context as AppCompatActivity).supportFragmentManager,
                                FRAG_CONFIRM_DELETE_SET
                            )
                        }

                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.INSERT_SET_BEFORE -> {
                            viewModel.currentlySelectedExerciseSet.value?.let { set ->
                                showCreateNewSet(set.position - 1)
                            }
                        }

                        EditSetsBottomSheetDialogFragment.EditSetsBottomSheetOption.INSERT_SET_AFTER -> {
                            viewModel.currentlySelectedExerciseSet.value?.let { set ->
                                showCreateNewSet(set.position + 1)
                            }
                        }
                    }
                    viewModel.onDismissBottomSheet()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dataObserver?.let { adapter.unregisterAdapterDataObserver(it) }
    }

    private fun showCreateNewSet(position: Int) {
        viewModel.setSelectedExerciseNameText("")
        val action =
            EditSetsFragmentDirections.actionEditSetsFragmentToCreateNewSetFragment(position)
        findNavController().navigate(action)
    }

    private fun setupFab() {
        (activity as? MainActivity)?.let { act ->
            act.findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
            act.fabClickedEvent.observe(viewLifecycleOwner, androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let {
                    showCreateNewSet(viewModel.getSetCount())
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sortSetsIcon -> {
                viewModel.confirmNewOrder()
                adapter.notifyDataSetChanged()
            }

            R.id.confirmSort -> {
                viewModel.turnOffSortMode()
                adapter.notifyDataSetChanged()
            }

            R.id.cancelSort -> {
                viewModel.cancelSortMode()
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onCustomExerciseAdded(event: CustomExerciseAddedEvent) {
        val toast = Toast.makeText(
            activity,
            "Custom exercise ${event.exerciseName} added to Exercise Bank.",
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 200)
        //    toast.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        toast.show()
    }
}
