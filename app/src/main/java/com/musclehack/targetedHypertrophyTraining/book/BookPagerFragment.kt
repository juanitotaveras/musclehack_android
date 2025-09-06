package com.musclehack.targetedHypertrophyTraining.book

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
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
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentBookPagerBinding
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import com.musclehack.targetedHypertrophyTraining.premium.PagerContentsAdapter
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by juanito on 1/9/2018.
 *
 */

class BookPagerFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<BookViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentBookPagerBinding

    /** We need to ignore our first page selection, since that is just the setup. */
    var pageSelectionCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentBookPagerBinding.inflate(inflater, container, false).apply {

        }
        return viewDataBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.bookItems.observe(viewLifecycleOwner, Observer { contents ->
            if (contents != null)
                setupPager(contents)
        })
        (activity as? MainActivity)?.clearFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_book, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.socialMedia -> {
                val sortCyclesMenuView = requireActivity().findViewById<View>(R.id.socialMedia)
                val popupMenu = PopupMenu(requireActivity(), sortCyclesMenuView)
                popupMenu.inflate(R.menu.menu_social_media)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { i ->
                    val itemID = i.itemId
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupPager(contents: PagerContents) {
        val adapter = PagerContentsAdapter(activity as FragmentActivity, contents)
        viewDataBinding.bookPager.adapter = adapter
        TabLayoutMediator(viewDataBinding.tabs, viewDataBinding.bookPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        viewDataBinding.bookPager.post {
            viewDataBinding.bookPager.setCurrentItem(viewModel.getSavedPagePosition(), false)
        }
        viewDataBinding.bookPager.isSaveEnabled = false
        viewDataBinding.bookPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (pageSelectionCount > 0) {
                    viewModel.onBookPageSelected(position)
                    (activity as? BottomNavigationActivity)?.forceBottomNavigationToAppear()
                }
                /** Stating again here for clarity. We should ignore the first onPageSelected()
                 * because it occurs when pager is created. */
                pageSelectionCount = ((pageSelectionCount + 1).rem(50))
                super.onPageSelected(position)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                viewModel.onBookPageScrolled(position)
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })
    }
}
