package com.musclehack.targetedHypertrophyTraining.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentAboutBinding
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import com.musclehack.targetedHypertrophyTraining.premium.PagerContentsAdapter
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class AboutFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AboutViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentAboutBinding
            .inflate(inflater, container, false).apply {}
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bioItems.observe(viewLifecycleOwner, Observer { contents ->
            contents?.let { setupPager(it) }
        })
        (activity as? MainActivity)?.clearFab()
    }

    private fun setupPager(contents: PagerContents) {
        val adapter = PagerContentsAdapter(activity as FragmentActivity, contents)
        viewDataBinding.viewPager.adapter = adapter
        TabLayoutMediator(viewDataBinding.tabs, viewDataBinding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }
}