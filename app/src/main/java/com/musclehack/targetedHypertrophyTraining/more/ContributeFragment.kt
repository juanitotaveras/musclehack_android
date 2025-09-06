package com.musclehack.targetedHypertrophyTraining.more

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentContributeBinding
import dagger.android.support.DaggerFragment


class ContributeFragment : DaggerFragment() {
    private var coffeeAnimation: AnimationDrawable? = null
    private lateinit var viewDataBinding: FragmentContributeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentContributeBinding.inflate(inflater, container, false).apply {
        }
        viewDataBinding.coffeeIcon.setBackgroundResource(R.drawable.coffee_animation)
        coffeeAnimation = viewDataBinding.coffeeIcon.background as AnimationDrawable
        return viewDataBinding.root
    }

    override fun onStart() {
        super.onStart()
        coffeeAnimation?.start()
    }
}