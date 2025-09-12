package com.musclehack.targetedHypertrophyTraining.testimonials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.musclehack.targetedHypertrophyTraining.BottomNavigationActivity
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentTestimonialsBinding
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import com.musclehack.targetedHypertrophyTraining.premium.PagerContentsAdapter
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TestimonialsFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<TestimonialsViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentTestimonialsBinding

    /** We need to ignore our first page selection, since that is just the setup. */
    var pageSelectionCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentTestimonialsBinding
            .inflate(inflater, container, false).apply {}
        viewDataBinding.swipePrompt.visibility =
            if (viewModel.shouldShowSwipePrompt()) View.VISIBLE else View.GONE
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        swipePrompt()
        viewDataBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (pageSelectionCount > 0) {
                    viewDataBinding.swipePrompt.visibility = View.GONE

                    viewModel.onBioPageSelected(position)
                    (activity as? BottomNavigationActivity)?.forceBottomNavigationToAppear()
                }
                pageSelectionCount = ((pageSelectionCount + 1).rem(50))
                super.onPageSelected(position)
            }
        })
    }

    private fun swipePrompt() {
        if (viewModel.shouldShowSwipePrompt()) {
            val anim = TranslateAnimation(
                0.toFloat(), -viewDataBinding.swipePrompt.width.toFloat() / 4,
                0.toFloat(), 0.toFloat()
            )
            anim.duration = 1000
            anim.repeatCount = 2
            viewDataBinding.swipePrompt.startAnimation(anim)
        }
    }
}