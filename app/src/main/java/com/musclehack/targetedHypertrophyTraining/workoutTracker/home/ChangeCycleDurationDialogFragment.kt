package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ChangeCycleDurationDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackerHomeViewModel> { viewModelFactory }

    private lateinit var form: View

    private var newDuration: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selectedCycle = viewModel.getSelectedCycle()
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_change_cycle_duration, null)

        val builder = AlertDialog.Builder(activity)

        val dialogHeader = resources.getString(R.string.change_cycle_duration)
        /* Listen for key changes. */
        val numWeeksTextBox = form.findViewById<EditText>(R.id.numWeeksTextBox)
        val enterNumWeeksWarningText = form.findViewById<TextView>(R.id.enterNumWeeksWarningText)
        numWeeksTextBox.setText(selectedCycle?.numWeeks.toString())
        numWeeksTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /* Set warning text if new duration is shorter than old one. */
                val st = s.toString()
                newDuration = if (st.isNotEmpty()) Integer.parseInt(st) else 0

                if (newDuration < selectedCycle?.numWeeks ?: 0) {
                    enterNumWeeksWarningText.visibility = View.VISIBLE
                    val diffWeeks = (selectedCycle?.numWeeks ?: 0) - newDuration
                    enterNumWeeksWarningText.text =
                        String.format(
                            resources.getString(R.string.shorten_cycle_duration_warning),
                            diffWeeks
                        )
                } else {
                    enterNumWeeksWarningText.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }


    override fun onClick(dialog: DialogInterface, which: Int) {
        viewModel.onConfirmChangeCycleDuration(newDuration)
    }
}
