package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME

class ExerciseBankOverlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            val exerciseName: String? = intent.getStringExtra(KEY_EXERCISE_NAME)
            if (exerciseName == null) {
                println("Exercise name could not be retrieved. Not showing bank overlay.")
                return
            }
            val frag = ExerciseBankSelectionOverlayFragment.newInstance(exerciseName)
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, frag).commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}
