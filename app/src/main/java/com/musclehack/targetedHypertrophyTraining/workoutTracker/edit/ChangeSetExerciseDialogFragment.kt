package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.exerciseBank.ExerciseBankOverlayActivity
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_SET_ID
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject


private const val CREATE_REQUEST = 4930

class ChangeSetExerciseDialogFragment : DaggerDialogFragment(),
    DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<EditSetsViewModel> { viewModelFactory }
    private val setID: Long?
        get() = arguments?.getLong(KEY_SET_ID, -1)
    private val oldExerciseName: String?
        get() = arguments?.getString(KEY_EXERCISE_NAME)

    private var form: View? = null
    private lateinit var exerciseNameTextBox: EditText

    /**
     * We don't want to remove this constructor, because this DialogFragment can be used by
     * two different view models, so the data would be inconsistent.
     * Considering keeping this constructor style on other dialog fragments, so viewModels
     * don't get too fat.
     */
    companion object {
        fun newInstance(setID: Long, exerciseName: String):
                ChangeSetExerciseDialogFragment {
            val frag = ChangeSetExerciseDialogFragment()
            val bundle = Bundle()
            bundle.putLong(KEY_SET_ID, setID)
            bundle.putString(KEY_EXERCISE_NAME, exerciseName)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = activity?.layoutInflater?.inflate(R.layout.dialog_change_set_exercise, null)
        exerciseNameTextBox = form!!.findViewById(R.id.exerciseNameTextBox)
        exerciseNameTextBox.setText(oldExerciseName)
        exerciseNameTextBox.hint = oldExerciseName
        val openExerciseBankButton = form!!.findViewById<Button>(R.id.openExerciseBankButton)
        openExerciseBankButton.setOnClickListener {
            /* Open exercise bank overlay activity. */
            val i = Intent(activity, ExerciseBankOverlayActivity::class.java)
            val newExerciseName = exerciseNameTextBox!!.text.toString()
            i.putExtra(KEY_EXERCISE_NAME, newExerciseName)
            startActivityForResult(i, CREATE_REQUEST)
        }

        val builder = AlertDialog.Builder(activity)
        val dialogHeader = getString(R.string.change_set_exercise_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val id = setID
        if (id != null) {
            viewModel.onChangeSetExercise(id, exerciseNameTextBox.text.toString())
        } else {
            // some error
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        intent: Intent?
    ) {
        if (requestCode == CREATE_REQUEST) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                intent?.let {
                    val exerciseName = it.getStringExtra(KEY_EXERCISE_NAME)
                    exerciseNameTextBox.setText(exerciseName)
                }
            }
        }
    }
}
