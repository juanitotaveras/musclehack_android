package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/3/2018.
 *
 */

class ChangeRepRangeDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }

    private lateinit var form: View
    private lateinit var minRepsTextBox: EditText
    private lateinit var maxRepsTextBox: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_change_rep_range, null)
        minRepsTextBox = form.findViewById(R.id.minRepsTextBox)
        maxRepsTextBox = form.findViewById(R.id.maxRepsTextBox)

        val exerciseSet: ExerciseSet? = viewModel.currentlySelectedExerciseSet.value
        if (exerciseSet != null) {
            minRepsTextBox.setText(exerciseSet.lowerReps.toString())
            minRepsTextBox.hint = exerciseSet.lowerReps.toString()
            maxRepsTextBox.setText(exerciseSet.higherReps.toString())
            maxRepsTextBox.hint = exerciseSet.higherReps.toString()
        }
        val builder = AlertDialog.Builder(activity)
        val dialogHeader = getString(R.string.change_rep_range_for_set)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    private fun parseEditText(box: EditText): Int {
        val input = box.text.toString()
        return if (input.isNotEmpty()) Integer.parseInt(input) else 0
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val newMinReps = parseEditText(minRepsTextBox)
        val newMaxReps = parseEditText(maxRepsTextBox)
        viewModel.onChangeRepRange(newMinReps, newMaxReps)
    }
}
