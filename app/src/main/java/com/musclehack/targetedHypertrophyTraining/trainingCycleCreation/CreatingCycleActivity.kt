package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.DaggerActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreatingCycleActivity : DaggerActivity(), ProgressPostedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CycleCreatorViewModel

    private lateinit var loadingImage: ImageView
    private lateinit var percentageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFactory.create(CycleCreatorViewModel::class.java)
        actionBar?.setDisplayUseLogoEnabled(false)
        setContentView(R.layout.activity_creating_cycle)

        title = String.format(
            getString(R.string.creating_cycle_title),
            viewModel.newCycleName
        )
        loadingImage = findViewById(R.id.loadingImage)
        percentageText = findViewById(R.id.percentageText)
        val deferred = viewModel.create()
        viewModel.viewModelScope.launch {
            deferred.await()
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
    }

    override fun onCycleCreated(cycleID: Int) {
        setResult(Activity.RESULT_OK, Intent())
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        prefs.edit()
//                .putInt(PREF_CURRENT_CYCLE_ID, cycleID)
//                .putString(PREF_CURRENT_CYCLE_NAME, cycleCreator?.newCycleName)
//                .putInt(PREF_CURRENT_CYCLE_DURATION, cycleCreator?.newCycleNumWeeks!!)
//                .putString(PREF_CURRENT_TRACKER_SCREEN, FRAG_TRACKER_WORKOUTS)
//                .apply()
        finish()
    }

    override fun onProgressPosted(percentage: Int) {
        val imageNum = StringBuilder(percentage.toString())
        while (imageNum.length < 3)
            imageNum.insert(0, "0")
        val resID = resources.getIdentifier(
            "loading$imageNum",
            "drawable",
            packageName
        )
        loadingImage.setImageResource(resID)
        val percent = "$percentage%"
        percentageText.text = percent
    }
}
