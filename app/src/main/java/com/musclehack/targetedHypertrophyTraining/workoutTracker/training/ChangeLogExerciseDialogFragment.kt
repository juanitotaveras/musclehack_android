package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.exerciseBank.ExerciseBankOverlayActivity
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

private const val CREATE_REQUEST = 4930

class ChangeLogExerciseDialogFragment : DaggerDialogFragment(),
    DialogInterface.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrainingViewModel> { viewModelFactory }

    private lateinit var form: View
    private lateinit var exerciseNameTextBox: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val logCard = viewModel.currentlySelectedLogCard.value
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_change_log_exercise, null)
        exerciseNameTextBox = form.findViewById(R.id.exerciseNameTextBox)

        exerciseNameTextBox.setText(logCard?.getExerciseName())
        exerciseNameTextBox.hint = logCard?.getExerciseName()
        val openExerciseBankButton = form.findViewById<Button>(R.id.openExerciseBankButton)
        openExerciseBankButton.setOnClickListener {
            /* Open exercise bank overlay activity. */
            val i = Intent(activity, ExerciseBankOverlayActivity::class.java)
            i.putExtra(KEY_EXERCISE_NAME, logCard?.getExerciseName())
            startActivityForResult(i, CREATE_REQUEST)
        }

        val builder = AlertDialog.Builder(activity)

        val dialogHeader = getString(R.string.change_log_exercise_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {

        val newName = exerciseNameTextBox.text.toString()
        // new Exercise name is current substitution
        val logCard = viewModel.currentlySelectedLogCard.value
        if (logCard?.getExerciseNameDefaultText() != newName) {
            viewModel.onChangeLogExerciseName(newName)
        } else
            noChangeNotification()

    }

    private fun noChangeNotification() {
        val toast = Toast.makeText(
            activity,
            getString(R.string.subsitution_failed_notification),
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.BOTTOM, 0, 100)
        toast.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == CREATE_REQUEST) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                val exerciseName = intent?.getStringExtra(KEY_EXERCISE_NAME)
                exerciseNameTextBox.setText(exerciseName)
            }
        }
    }
}
