package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/3/2018.
 *
 */
class ConfirmSkipDayDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }
    private lateinit var form: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_skip_day, null)
        val workout = viewModel.getArgs()?.second
        val repeats = workout?.repeats
        val day = viewModel.currentDay.get()
        val builder = AlertDialog.Builder(activity)

        var dialogHeader = resources.getString(R.string.skip_this_day)
        if (day != null && repeats != null) {
            val dayStr = if (repeats > 1)
                "${day / repeats + 1} (Day ${day % repeats + 1} of $repeats)"
            else
                "${day + 1}"
            dialogHeader = String.format(
                resources.getString(R.string.skip_day_dialog_header),
                dayStr
            )
        }

        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }


    override fun onClick(dialog: DialogInterface, which: Int) {
        // Saved preference
        val doNotShowCheckBox: CheckBox = form.findViewById(R.id.doNotShowSkipDayWarningCheckBox)
        if (doNotShowCheckBox.isChecked)
            viewModel.setShouldNotShowSkipDayDialog()
        viewModel.onSkipDay()
    }

}
