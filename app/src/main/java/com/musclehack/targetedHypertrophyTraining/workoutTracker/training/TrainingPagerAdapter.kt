/*
 * Author: Juanito Taveras
 * Created: 11/25/17
 * Modified: 11/25/17
 *
 */
package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TrainingPagerAdapter(fa: FragmentActivity, private val viewModel: TrainingViewModel) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return viewModel.getPageCount()
    }

    override fun createFragment(position: Int): Fragment {
        return LogPageFragment.newInstance(position)
    }
}
