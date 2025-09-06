package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentExerciseBankBinding
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_EXERCISE_BANK_INFO_MODAL
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/** Fragment is created by an activity. This fragment is the overlay that appears
 * on any TrainingPager fragment, and when replacing an exercise in EditSets. */
class ExerciseBankSelectionOverlayFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<ExerciseBankSelectionViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentExerciseBankBinding
    private lateinit var fullListFilterAdapter: ExerciseBankFilterAdapter

    private val oldExerciseName: String?
        get() = arguments?.getString(KEY_EXERCISE_NAME)

    companion object {
        fun newInstance(oldExerciseName: String): ExerciseBankSelectionOverlayFragment {
            val frag = ExerciseBankSelectionOverlayFragment()
            val bundle = Bundle()
            bundle.putString(KEY_EXERCISE_NAME, oldExerciseName)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.start(activity)
        setupListAdapter()
        viewModel.getDefaultExercises()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { df ->
                if (df != null) {
                    setupListAdapter()
                }
            })
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.exercise_bank_overlay_title)
        unfocusAutoFill()
        setupNavigation()
    }

    private fun setupNavigation() {
        viewModel.exerciseChosenEvent.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { event ->
                event.getContentIfNotHandled()?.let { exerciseName ->
                    val i = Intent()
                    i.putExtra(KEY_EXERCISE_NAME, exerciseName)
                    activity?.setResult(Activity.RESULT_OK, i)
                    activity?.finish()
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
        return super.onOptionsItemSelected(item)
    }

    private fun unfocusAutoFill() {
        val container = requireActivity().findViewById<LinearLayout>(R.id.exerciseBankLinearLayout)
        container.requestFocus()
    }

    private fun expandCategory(previousExerciseName: String, defExercises: DefaultExercises) {
        // expand item that matches category
        var match: String? = null
        // iterate listDataChild
        for ((key, value) in defExercises.exercises) {
            for (v in value) {
                if (v.exerciseName == previousExerciseName) {
                    match = key
                    break
                }
            }
        }
        if (match != null) {
            // get match idx
            val matchIdx = defExercises.categories.indexOf(match)
            viewDataBinding.expandableListView.expandGroup(matchIdx)
        }
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null && viewModel.getDefaultExercises().value != null) {
            val adapter = ExpandableListAdapter(viewModel = viewModel)
            viewDataBinding.expandableListView.setAdapter(adapter)
            oldExerciseName?.let { exerciseName ->
                val df = viewModel.getDefaultExercises().value ?: return
                expandCategory(previousExerciseName = exerciseName, defExercises = df)
            }

        } else {
            // log here!
        }
    }
}