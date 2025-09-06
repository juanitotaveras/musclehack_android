package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

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

class ConfirmDeleteCycleDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var form: View? = null
    private val viewModel by activityViewModels<TrackerHomeViewModel> { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selectedCycle = viewModel.getSelectedCycle()
        form = activity?.layoutInflater
            ?.inflate(R.layout.confirm_delete_cycle_dialog, null)

        val builder = AlertDialog.Builder(activity)
        val deleteMessage = (resources.getString(R.string.confirm_delete_cycle_title)
                + " " + selectedCycle?.name + resources.getString(R.string.question_mark))
        return builder.setTitle(deleteMessage)//.setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        viewModel.onConfirmDeleteCycle()
    }
}
