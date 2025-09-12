package com.musclehack.targetedHypertrophyTraining.more

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val view = viewDataBinding.root
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            // Return CONSUMED if you don't want the window insets to keep passing down
            // to descendant views.
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        coffeeAnimation?.start()
    }
}