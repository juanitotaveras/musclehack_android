package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentCreateNewSetBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CreateNewSetFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentCreateNewSetBinding
    private val args: CreateNewSetFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentCreateNewSetBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        viewDataBinding.openExerciseBankButton.setOnClickListener {
            showExerciseBank()
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.findViewById<FloatingActionButton>(R.id.fab)?.visibility =
            View.GONE
    }

    private fun showExerciseBank() {
        val action =
            CreateNewSetFragmentDirections.actionCreateNewSetFragmentToExerciseBankSelectionFragment()
        findNavController().navigate(action)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_create_new_set, menu)
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createSet) {
            /* get text from textbox */
            val exerciseName = viewDataBinding.exerciseNameText.text.toString()
            val minRepsStr = viewDataBinding.minRepsText.text.toString()
            val minReps = if (minRepsStr.isEmpty()) 8 else Integer.parseInt(minRepsStr)
            val maxRepsStr = viewDataBinding.maxRepsText.text.toString()
            val maxReps = if (maxRepsStr.isEmpty()) 12 else Integer.parseInt(maxRepsStr)
            val restTimeStr = viewDataBinding.restTimeText.text.toString()
            val restTime = if (restTimeStr.isEmpty()) 120 else Integer.parseInt(restTimeStr)
            // TODO: Do error checking here
            val numSetsStr = viewDataBinding.numberSetsText.text.toString()
            val numSets = if (numSetsStr.isEmpty()) 1 else Integer.parseInt(numSetsStr)
            if (numSets in 1..20) {
                var newPosition = args.position
                if (newPosition < 0) {
                    newPosition = 0
                } else if (newPosition > viewModel.getSetCount()) {
                    newPosition = viewModel.getSetCount() - 1
                }
                viewModel.onAddExerciseSets(
                    newPosition, exerciseName,
                    minReps, maxReps, restTime, numSets
                )
            } else {
                // show toast
                // TODO: Make string resource
                val toast = Toast.makeText(
                    activity,
                    "Number of sets must be greater than 0 and less than 21.",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 200)
                toast.show()
            }
            hideSoftKeyboard(viewDataBinding.constraintLayout)
            activity?.onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }
}
