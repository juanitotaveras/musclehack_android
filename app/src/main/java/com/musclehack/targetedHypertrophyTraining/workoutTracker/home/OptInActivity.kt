package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.SimpleContentFragment
import com.musclehack.targetedHypertrophyTraining.utilities.EXTRA_FILE

class OptInActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayUseLogoEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.opt_in_title)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            val file = intent.getStringExtra(EXTRA_FILE)
            if (file == null) {
                println("EXTRA_FILE not retrieved. Not loading OptInActivity.")
                return
            }
            val f = SimpleContentFragment.newInstance(file, false)

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, f).commit()
        }
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}