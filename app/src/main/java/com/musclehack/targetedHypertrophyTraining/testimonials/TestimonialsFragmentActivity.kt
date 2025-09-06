package com.musclehack.targetedHypertrophyTraining.testimonials

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.FRAG_TESTIMONIALS

class TestimonialsFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(FRAG_TESTIMONIALS) == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, TestimonialsFragment(), FRAG_TESTIMONIALS)
                .commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setTitle(R.string.testimonials_title)
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}
