package com.musclehack.targetedHypertrophyTraining.premium

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.musclehack.targetedHypertrophyTraining.SimpleContentFragment

/**
 * Created by juanito on 1/10/2018.
 *
 */

class PagerContentsAdapter(fa: FragmentActivity, private val contents: PagerContents) :
    FragmentStateAdapter(fa) {

    fun getPageTitle(position: Int): CharSequence = contents.getItemTitle(position)!!
    override fun getItemCount(): Int {
        return contents.itemCount
    }

    override fun createFragment(position: Int): Fragment {
        return SimpleContentFragment.newInstance(contents.getItemPath(position), false)
    }
}
