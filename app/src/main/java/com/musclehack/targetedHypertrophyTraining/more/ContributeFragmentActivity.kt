package com.musclehack.targetedHypertrophyTraining.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_CONTRIBUTE

class ContributeFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(FRAG_CONTRIBUTE) == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, ContributeFragment(), FRAG_CONTRIBUTE)
                .commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setTitle(R.string.contribute_title)
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}