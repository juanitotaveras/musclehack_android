package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.musclehack.targetedHypertrophyTraining.BottomNavigationActivity
import com.musclehack.targetedHypertrophyTraining.EventObserver
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentTrackerHomeBinding
import com.musclehack.targetedHypertrophyTraining.utilities.EXTRA_FILE
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CHANGE_CYCLE_DURATION
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CONFIRM_DELETE_CYCLE
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_PLEASE_RATE
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_RENAME_CYCLE
import com.musclehack.targetedHypertrophyTraining.utilities.PleaseRateDialogFragment
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class TrackerHomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var listAdapter: CyclesAdapter2
    private lateinit var viewDataBinding: FragmentTrackerHomeBinding
    private val viewModel by activityViewModels<TrackerHomeViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (viewModel.shouldAskUserToRateApp()) {
            PleaseRateDialogFragment().show(requireFragmentManager(), FRAG_PLEASE_RATE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tracker_drawer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reorderCycles -> {
                (activity as? AppCompatActivity)?.let { activity ->
                    val sortCyclesMenuView = activity.findViewById<View>(R.id.reorderCycles)
                    val popupMenu = PopupMenu(activity, sortCyclesMenuView)
                    popupMenu.inflate(R.menu.menu_sort_cycles)
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                        val itemID = it.itemId
                        when (itemID) {
                            R.id.sortByMRU -> {
                                viewModel.sortCyclesByDateUsed()
                                return@OnMenuItemClickListener true
                            }

                            R.id.sortByDateCreated -> {
                                viewModel.sortCyclesByDateCreated()
                                return@OnMenuItemClickListener true
                            }
                        }

                        false
                    })
                    popupMenu.show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupNavigation() {
        viewModel.openCycleEvent.observe(this.viewLifecycleOwner, EventObserver {
            openCycle(it)
        })
        setupBottomSheet()
        setupFab()
    }

    private fun setupBottomSheet() {
        viewModel.openBottomSheetEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                val ctxt = context as AppCompatActivity
                TrackerHomeBottomDialogFragment().show(ctxt.supportFragmentManager, "tag")
            }
        })
        viewModel.bottomSheetItemSelectedEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                val ctxt = context as? AppCompatActivity ?: return@Observer
                when (it) {
                    TrackerHomeViewModel.BottomSheetMenuOption.RENAME_CYCLE -> {
                        RenameCycleDialogFragment().show(
                            ctxt.supportFragmentManager,
                            FRAG_RENAME_CYCLE
                        )
                    }

                    TrackerHomeViewModel.BottomSheetMenuOption.DELETE_CYCLE -> {
                        ConfirmDeleteCycleDialogFragment().show(
                            ctxt.supportFragmentManager,
                            FRAG_CONFIRM_DELETE_CYCLE
                        )
                    }

                    TrackerHomeViewModel.BottomSheetMenuOption.CHANGE_CYCLE_DURATION -> {
                        ChangeCycleDurationDialogFragment().show(
                            ctxt.supportFragmentManager,
                            FRAG_CHANGE_CYCLE_DURATION
                        )
                    }

                    TrackerHomeViewModel.BottomSheetMenuOption.CLONE_CYCLE -> {
                        viewModel.cloneCycle()
                    }
                }
                viewModel.onDismissBottomSheet()
            }
        })
    }

    private fun setupFab() {
        (activity as? MainActivity)?.let { act ->
            act.findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
            act.fabClickedEvent.observe(viewLifecycleOwner, Observer { event ->
                event.getContentIfNotHandled()?.let {
                    val action = TrackerHomeFragmentDirections
                        .actionTrackerHomeFragmentToCreateNewCycleSelectTemplateFragment()
                    findNavController().navigate(action)
                }
            })
        }
    }

    private fun openCycle(cycleId: Long) {
        val action = TrackerHomeFragmentDirections
            .actionTrackerHomeFragmentToTrackerWorkoutsFragmentDest(cycleId)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentTrackerHomeBinding
            .inflate(inflater, container, false).apply {
                viewmodel = viewModel
            }
        viewDataBinding.optInButton.setOnClickListener {
            val i = Intent(context, OptInActivity::class.java)
            i.putExtra(EXTRA_FILE, "file:///android_asset/misc/optin.html")
            startActivity(i)
        }
        viewDataBinding.helpWebView.visibility = View.GONE
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupListAdapter()
        setupNavigation()
        (activity as? BottomNavigationActivity)?.forceBottomNavigationToAppear()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = CyclesAdapter2(viewModel = viewModel)
            viewDataBinding.cycleTrackerHomeList.adapter = listAdapter
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            viewDataBinding.cycleTrackerHomeList.addItemDecoration(divider)
        } else {
            // log here!
        }
    }

}
