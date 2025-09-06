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
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_CYCLE_ID
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_SET_ID
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_WORKOUT_ID
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOW_DELETE_SET_WARNING
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ConfirmDeleteSetDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }
    private lateinit var form: View

    companion object {
        fun newInstance(cycleID: Long, workoutID: Long, setID: Long):
                ConfirmDeleteSetDialogFragment {
            val frag = ConfirmDeleteSetDialogFragment()
            val bundle = Bundle()
            bundle.putLong(KEY_CYCLE_ID, cycleID)
            bundle.putLong(KEY_WORKOUT_ID, workoutID)
            bundle.putLong(KEY_SET_ID, setID)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_confirm_delete_set, null)

        val builder = AlertDialog.Builder(activity)

        val dialogHeader = getString(R.string.delete_set_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        viewModel.onDeleteSet()
        val doNotShowCheckBox: CheckBox = form.findViewById(R.id.doNotShowDeleteSetWarningCheckBox)
        if (doNotShowCheckBox.isChecked)
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_SHOW_DELETE_SET_WARNING, false).apply()
    }

}
