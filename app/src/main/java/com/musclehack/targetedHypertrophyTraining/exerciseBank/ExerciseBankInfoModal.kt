package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.musclehack.targetedHypertrophyTraining.R

class ExerciseBankInfoModal : androidx.fragment.app.DialogFragment(),
    DialogInterface.OnClickListener {
    private lateinit var form: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.exercise_bank_info_dialog, null)
        val builder = android.app.AlertDialog.Builder(activity)
        val dialogHeader = resources.getString(R.string.exercise_bank_info_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {}
}
