package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.databinding.DialogLogNoteBinding
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/3/2018.
 *
 */

class EditLogNoteDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: DialogLogNoteBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewDataBinding = DialogLogNoteBinding.inflate(LayoutInflater.from(context))
        val exerciseNote = viewModel.currentlyEditingNote.value
        var dialogHeader = "Record a note"
        if (exerciseNote != null && exerciseNote.note.isNotEmpty()) {
            viewDataBinding.noteTextBox.setText(exerciseNote.note)
            viewDataBinding.noteTextBox.hint = exerciseNote.note
            dialogHeader = "Edit note"
        }

        val builder = AlertDialog.Builder(activity)
        return builder.setTitle(dialogHeader).setView(viewDataBinding.root)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val newNote = viewDataBinding.noteTextBox.text.toString()
        viewModel.onNoteSaved(newNote)
    }
}
