package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ChooseSubstitutionTypeDialogFragment : DaggerDialogFragment(),
    DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }

    private lateinit var form: View
    private lateinit var subSelectionRadioGroup: RadioGroup

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_choose_substitute_type, null)
        subSelectionRadioGroup = form.findViewById(R.id.chooseSubRadioGroup)
        val singleDayRadio: RadioButton = form.findViewById(R.id.radioChangeForThisDay)
        singleDayRadio.isChecked = true
        val builder = AlertDialog.Builder(activity)
        val dialogHeader = getString(R.string.choose_ex_sub_type_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        when (subSelectionRadioGroup.checkedRadioButtonId) {
            R.id.radioChangeForThisDay -> {
                viewModel.onExerciseSubTypeSelected(
                    ExerciseSubstitutionType.SINGLE_DAY
                )
            }

            R.id.radioChangeForEntireCycle -> {
                // show dialog 2
                viewModel.onExerciseSubTypeSelected(
                    ExerciseSubstitutionType.ENTIRE_CYCLE
                )
            }
        }
    }
}