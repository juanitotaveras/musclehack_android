package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CreateNewWorkoutFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackerWorkoutsViewModel> { viewModelFactory }

    private var workoutNameText: EditText? = null
    private var repeatsText: EditText? = null
    private lateinit var containerLayout: ConstraintLayout
    private lateinit var repeatInputWarningText: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.clearFab()
    }

    private class InputFilterMinMax(val min: Int, val max: Int, val warningText: TextView) :
        InputFilter {
        override fun filter(
            source: CharSequence?, start: Int, end: Int, dest: Spanned?,
            dstart: Int, dend: Int
        ): CharSequence? {
            try {
                var newVal = dest.toString().substring(0, dstart) +
                        dest.toString().substring(dend, dest.toString().length)
                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(
                    dstart,
                    newVal.length
                )
                val input = Integer.parseInt(newVal)
                if (isInRange(min, max, input)) {
                    // correct range
                    if (warningText.visibility == View.VISIBLE) {
                        warningText.visibility = View.INVISIBLE
                    }
                    return null
                }
            } catch (nfe: NumberFormatException) {
            }
            if (warningText.visibility == View.INVISIBLE)
                warningText.visibility = View.VISIBLE
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (c in a..b) b > a else (c in b..a)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.fragment_create_new_workout, container,
            false
        )
        repeatInputWarningText = view.findViewById(R.id.repeatInputWarningText)
        repeatsText = view.findViewById(R.id.workout_repeats_txt)
        repeatsText!!.filters = arrayOf<InputFilter>(
            InputFilterMinMax(
                1, 7,
                repeatInputWarningText
            )
        )
        workoutNameText = view.findViewById(R.id.workout_name_txt)
        containerLayout = view.findViewById(R.id.createNewWorkoutLayout)
        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_create_new_workout, menu)
    }

    private fun dismissFragment() {
        // collapse keyboard if it is active
        hideSoftKeyboard(containerLayout)
        activity?.onBackPressed()
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent host in AndroidManifest.xml.

        if (item.itemId == R.id.create_workout) {
            /* get text from textbox */
            val workoutName = workoutNameText!!.text.toString()
            /* Place repeats here as well */
            val repeatsVal = repeatsText!!.text.toString()
            val repeats = if (repeatsVal.isNotEmpty()) Integer.parseInt(repeatsVal) else 1
            val workout = Workout(
                name = workoutName, id = 0, position = 0, repeats = repeats, lastDayViewed = 0,
                lastSetViewed = 0, cycleId = 0
            )
            viewModel.onCreateNewWorkout(workout)
            dismissFragment()
        }
        return super.onOptionsItemSelected(item)
    }
}
