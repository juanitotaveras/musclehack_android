package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.databinding.DialogPrevLogNoteBinding
import dagger.android.support.DaggerDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PreviousLogNoteDialogFragment : DaggerDialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pageTitleHelper: PageTitleHelper
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: DialogPrevLogNoteBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewDataBinding = DialogPrevLogNoteBinding.inflate(LayoutInflater.from(context))
        val exerciseNote = viewModel.currentlyEditingNote.value
        viewDataBinding.noteTextBox.text = exerciseNote?.note

        if (exerciseNote != null) {
            val ft = SimpleDateFormat("E MMM d y", Locale.US)
            viewDataBinding.dateLabel.text = ft.format(Date(exerciseNote.date))
            val frequency = viewModel.getArgs()?.second?.repeats
            val workoutName = viewModel.getArgs()?.second?.name
            if (frequency != null && workoutName != null) {
                viewDataBinding.weekLabel.text =
                    pageTitleHelper.getPageTitleForTab(exerciseNote.day, frequency, workoutName)
            }
        }
        val builder = AlertDialog.Builder(activity)
        // TODO: make string resource
        val dialogHeader = "Last recorded note"//res.getString("Change exercise");
        return builder.setTitle(dialogHeader).setView(viewDataBinding.root)
            .setPositiveButton(android.R.string.ok, this).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        dismiss()
    }
}
