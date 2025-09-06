/*
Author: Juanito Taveras
Created: 11/24/17
Modified: 11/24/17
 */

package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.exerciseBank.ExerciseBankMainViewModel
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOW_DELETE_EXERCISE_BANK_ITEM_WARNING
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ConfirmDeleteExerciseDialogFragment : DaggerDialogFragment(),
    DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<ExerciseBankMainViewModel> { viewModelFactory }
    private lateinit var form: View

    private val newExerciseName: String
        get() = requireArguments().getString(KEY_EXERCISE_NAME)!!

    companion object {
        fun newInstance(exerciseName: String): ConfirmDeleteExerciseDialogFragment {
            val bundle = Bundle()
            bundle.putString(KEY_EXERCISE_NAME, exerciseName)
            val frag = ConfirmDeleteExerciseDialogFragment()
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_confirm_delete_exercise, null)

        val builder = AlertDialog.Builder(activity)

        val deleteExerciseHeader = String.format(
            getString(R.string.delete_exercise_bank_item_header), newExerciseName
        )
        return builder.setTitle(deleteExerciseHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        // Saved preference
        val doNotShowCheckBox: CheckBox =
            form.findViewById(R.id.doNotShowDeleteExerciseWarningCheckBox)
        if (doNotShowCheckBox.isChecked)
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_SHOW_DELETE_EXERCISE_BANK_ITEM_WARNING, false).apply()
        viewModel.deleteCustomExercise(newExerciseName)
    }
}
