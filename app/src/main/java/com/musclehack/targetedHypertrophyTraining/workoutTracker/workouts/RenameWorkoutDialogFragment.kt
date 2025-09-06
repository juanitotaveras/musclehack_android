package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

/**
 * Created by juanito on 12/28/2017.
 *
 */
class RenameWorkoutDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackerWorkoutsViewModel> { viewModelFactory }

    private lateinit var form: View
    private lateinit var renameEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /* Should be the same as dialog_rename_workout, so we can use the same one. */
        val workout = viewModel.currentlySelectedWorkout.value
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_rename_cycle, null)
        renameEditText = form.findViewById(R.id.renameCycleEditText)
        if (workout != null) {
            renameEditText.setText(workout.name)
            renameEditText.hint = workout.name
        }
        val builder = AlertDialog.Builder(activity)
        val renameText = resources.getString(R.string.rename_workout)
        renameEditText.setSelectAllOnFocus(true)
        renameEditText.requestFocus()
        val d = builder.setTitle(renameText).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
        val window = d.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return d
    }


    override fun onClick(dialog: DialogInterface, which: Int) {
        val newWorkoutName = renameEditText.text.toString()
        viewModel.onWorkoutRenamed(newWorkoutName)
    }
}
