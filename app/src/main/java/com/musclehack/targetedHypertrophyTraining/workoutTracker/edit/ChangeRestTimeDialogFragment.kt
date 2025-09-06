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
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/3/2018.
 *
 */

class ChangeRestTimeDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }
    private lateinit var form: View
    private lateinit var restTimeTextBox: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val exerciseSet = viewModel.currentlySelectedExerciseSet.value
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_change_rest_time, null)
        restTimeTextBox = form.findViewById(R.id.restTimeTextBox)
        if (exerciseSet != null) {
            restTimeTextBox.setText("${exerciseSet.restTime}")
            restTimeTextBox.hint = "${exerciseSet.restTime}"
        }
        val builder = AlertDialog.Builder(activity)
        val dialogHeader = getString(R.string.change_rest_time_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    private fun parseEditText(box: EditText): Int {
        val input = box.text.toString()
        return if (input.isNotEmpty()) Integer.parseInt(input) else 0
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val restTime = parseEditText(restTimeTextBox)
        viewModel.onChangeRestTime(restTime)
    }
}
