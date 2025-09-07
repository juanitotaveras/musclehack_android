package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentExerciseBankBinding
import com.musclehack.targetedHypertrophyTraining.utilities.BrowserUtils
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CONFIRM_DELETE_CUSTOM_EX
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_EXERCISE_BANK_INFO_MODAL
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ConfirmDeleteExerciseDialogFragment
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * This is the fragment used by our Exercise Bank tab.
 */
class ExerciseBankMainFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<ExerciseBankMainViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentExerciseBankBinding
    private lateinit var fullListFilterAdapter: ExerciseBankFilterAdapter
    private var listAdapter: ExpandableListAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.start()
        (activity as? MainActivity)?.findViewById<FloatingActionButton>(R.id.fab)?.visibility =
            View.GONE
        setupListAdapter()
        viewModel.getDefaultExercises()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { df ->
                if (df != null) {
                    setupListAdapter()
                }
            })
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.exercise_bank_title)
        setupNavigation()
        setupSnackbar()
    }

    private fun setupSnackbar() {

    }

    private fun setupNavigation() {
        viewModel.openExerciseLinkEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { link ->
                    BrowserUtils.openUrlInChromeCustomTabs(activity, link)
                }
            })
        viewModel.showConfirmDeleteExerciseEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { name ->
                    val confirmDeleteExerciseFrag = ConfirmDeleteExerciseDialogFragment
                        .newInstance(exerciseName = name)
                    confirmDeleteExerciseFrag.show(
                        requireFragmentManager(),
                        FRAG_CONFIRM_DELETE_CUSTOM_EX
                    )
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentExerciseBankBinding
            .inflate(inflater, container, false).apply {
                viewmodel = viewModel
            }
        /** Add padding so we stay above the bottom navigation view. */
        val bottomNavHeight =
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.height
                ?: 0
        viewDataBinding.expandableListView.setPadding(0, 0, 0, bottomNavHeight)

        setupTextSearch()
        return viewDataBinding.root
    }

    private fun setupTextSearch() {
        viewDataBinding.searchIcon.visibility = View.VISIBLE
        viewDataBinding.autoCompleteBox.visibility = View.VISIBLE
        fullListFilterAdapter = ExerciseBankFilterAdapter(viewModel = viewModel)
        viewDataBinding.exerciseBankFullList.adapter = fullListFilterAdapter
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        viewDataBinding.exerciseBankFullList.addItemDecoration(divider)
        viewDataBinding.autoCompleteBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /* Set warning text if new duration is shorter than old one. */
                val st = s.toString()
                viewModel.setBankSearchText(st)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        viewModel.getBankSearchText().observe(viewLifecycleOwner, Observer { st ->
            if (st.isNotEmpty()) {
                if (viewDataBinding.exerciseBankFullList.visibility == View.GONE) {
                    viewDataBinding.exerciseBankFullList.visibility = View.VISIBLE
                    viewDataBinding.expandableListView.visibility = View.GONE
                }
            } else {
                if (viewDataBinding.exerciseBankFullList.visibility == View.VISIBLE) {
                    viewDataBinding.exerciseBankFullList.visibility = View.GONE
                    viewDataBinding.expandableListView.visibility = View.VISIBLE
                }
            }
        })
        viewModel.getFilteredItems().observe(viewLifecycleOwner, Observer { list ->
            (viewDataBinding.exerciseBankFullList.adapter as? ExerciseBankFilterAdapter)?.submitList(
                list
            )
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_exercise_bank, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.info -> {
                fragmentManager?.let { mgr ->
                    ExerciseBankInfoModal().show(mgr, FRAG_EXERCISE_BANK_INFO_MODAL)
                }
            }
        }
        return true
    }

    private fun unfocusAutoFill() {
        val container = requireActivity().findViewById<LinearLayout>(R.id.exerciseBankLinearLayout)
        container.requestFocus()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null && viewModel.getDefaultExercises().value != null) {
            listAdapter =
                if (listAdapter == null) ExpandableListAdapter(viewModel = viewModel) else listAdapter
            viewDataBinding.expandableListView.setAdapter(listAdapter)
            viewModel.getExerciseDeletedEvent().observe(viewLifecycleOwner, Observer { event ->
                event.getContentIfNotHandled()?.let {
                    listAdapter?.notifyDataSetChanged()
                }
            })
        } else {
            // log here!
        }
    }
}
