package com.musclehack.targetedHypertrophyTraining.premium

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.musclehack.targetedHypertrophyTraining.BottomNavigationActivity
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentProductsPagerBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/9/2018.
 *
 */

class ProductsPagerFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<ProductsViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentProductsPagerBinding

    /** We need to ignore our first page selection, since that is just the setup. */
    var pageSelectionCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentProductsPagerBinding.inflate(inflater, container, false).apply {

        }
        viewDataBinding.swipePrompt.visibility =
            if (viewModel.shouldShowSwipePrompt()) View.VISIBLE else View.GONE
        return viewDataBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.productItems.observe(viewLifecycleOwner, Observer { contents ->
            contents?.let { setupPager(it) }
        })
        (activity as? MainActivity)?.clearFab()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_products_page, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.socialMedia -> {
                val sortCyclesMenuView = requireActivity().findViewById<View>(R.id.socialMedia)
                val popupMenu = PopupMenu(activity, sortCyclesMenuView)
                popupMenu.inflate(R.menu.menu_social_media)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    val itemID = item.itemId
                    when (itemID) {
                        R.id.facebookOption -> {
                            requireActivity().startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.facebook.com/MuscleHack")
                                )
                            )
                            return@OnMenuItemClickListener true
                        }

                        R.id.instagramOption -> {
                            requireActivity().startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://instagram.com/mark_mc_manus/")
                                )
                            )
                            return@OnMenuItemClickListener true
                        }

                        R.id.twitterOption -> {
                            requireActivity().startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://twitter.com/MuscleHacker")
                                )
                            )
                            return@OnMenuItemClickListener true
                        }
                    }

                    false
                })

                val menuHelper: Any
                val argTypes: Array<Class<*>>
                /* enables icons in PopupMenu */

                try {
                    val fMenuHelper = PopupMenu::class.java.getDeclaredField("mPopup")
                    fMenuHelper.isAccessible = true
                    menuHelper = fMenuHelper.get(popupMenu)
                    argTypes = arrayOf(Boolean::class.javaPrimitiveType!!)
                    menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                        .invoke(menuHelper, true)
                } catch (e: Exception) {
//                    Log.e("warning: ", "icon failed to show.")
                }
                popupMenu.show()
            }

            R.id.becomeAffiliate -> {
                requireActivity().startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.musclehack.com/become-a-musclehack-affiliate/")
                    )
                )
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun setupPager(contents: PagerContents) {
        val adapter = PagerContentsAdapter(activity as FragmentActivity, contents)
        viewDataBinding.productsPager.adapter = adapter
        TabLayoutMediator(viewDataBinding.tabs, viewDataBinding.productsPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        viewDataBinding.productsPager.isSaveEnabled = false
        swipePrompt()
        viewDataBinding.productsPager.post {
            viewDataBinding.productsPager.setCurrentItem(viewModel.getSavedProductPosition(), false)
        }
        viewDataBinding.productsPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (pageSelectionCount > 0) {
                    viewDataBinding.swipePrompt.visibility = View.GONE

                    viewModel.onProductPageSelected(position)
                    (activity as? BottomNavigationActivity)?.forceBottomNavigationToAppear()
                }
                pageSelectionCount = ((pageSelectionCount + 1).rem(50))
                super.onPageSelected(position)
            }
        })
    }
}
