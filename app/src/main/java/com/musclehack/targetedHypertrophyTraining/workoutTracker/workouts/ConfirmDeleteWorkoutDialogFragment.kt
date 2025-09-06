/*
Author: Juanito Taveras
Created: 11/24/17
Modified: 11/24/17
 */

package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ConfirmDeleteWorkoutDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackerWorkoutsViewModel> { viewModelFactory }
    private lateinit var form: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.confirm_delete_workout_dialog, null)

        val builder = AlertDialog.Builder(activity)

        val workout = viewModel.currentlySelectedWorkout.value
        val workoutName = workout?.name ?: ""
        val deleteMessage = (resources.getString(R.string.confirm_delete_cycle_title)
                + " " + workoutName + resources.getString(R.string.question_mark))
        return builder.setTitle(deleteMessage).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        viewModel.onWorkoutDeleted()
    }
}
