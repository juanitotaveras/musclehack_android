package com.musclehack.targetedHypertrophyTraining.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_ABOUT

class AboutFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(FRAG_ABOUT) == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, AboutFragment(), FRAG_ABOUT)
                .commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setTitle(R.string.about_title)
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}