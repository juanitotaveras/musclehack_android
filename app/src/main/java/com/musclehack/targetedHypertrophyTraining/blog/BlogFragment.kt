package com.musclehack.targetedHypertrophyTraining.blog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentBlogBinding
import com.musclehack.targetedHypertrophyTraining.utilities.BrowserUtils
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class BlogFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<BlogViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentBlogBinding

    @Inject
    lateinit var appContext: Context

    private lateinit var listAdapter: PostsAdapter2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
        (activity as? MainActivity)?.findViewById<FloatingActionButton>(R.id.fab)?.visibility =
            View.GONE
        setupNavigation()
        viewModel.start()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = PostsAdapter2(viewModel = viewModel)
            viewDataBinding.postsList.adapter = listAdapter
            viewDataBinding.swipeRefreshLayout.isRefreshing = true
            viewModel.posts.observe(viewLifecycleOwner, androidx.lifecycle.Observer { posts ->
                if (posts != null && viewDataBinding.swipeRefreshLayout.isRefreshing) {
                    viewDataBinding.swipeRefreshLayout.isRefreshing = false
                }
                (viewDataBinding.postsList.adapter as PostsAdapter2).submitList(posts)
            })
            viewDataBinding.swipeRefreshLayout.setOnRefreshListener {
                viewDataBinding.swipeRefreshLayout.isRefreshing = true
                viewModel.forceRefresh()
            }
        } else {
            // log here!
        }
    }

    private fun setupNavigation() {
        viewModel.postClickedEvent.observe(
            viewLifecycleOwner
        ) { event ->
            event.getContentIfNotHandled()?.let { link ->
                context?.let { ctxt ->
                    BrowserUtils.openUrlInChromeCustomTabs(ctxt, link)
                }
            }
        }
        viewModel.openNotificationLinkEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { link ->
                context?.let { ctxt ->
                    BrowserUtils.openUrlInChromeCustomTabs(ctxt, link)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_blog, menu)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentBlogBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.socialMedia -> {
                val sortCyclesMenuView = activity?.findViewById<View>(R.id.socialMedia)
                val popupMenu = PopupMenu(requireActivity(), sortCyclesMenuView)
                popupMenu.inflate(R.menu.menu_social_media)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.facebookOption -> {
                            activity?.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.facebook.com/MuscleHack")
                                )
                            )
                            return@OnMenuItemClickListener true
                        }

                        R.id.instagramOption -> {
                            activity?.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://instagram.com/mark_mc_manus/")
                                )
                            )
                            return@OnMenuItemClickListener true
                        }

                        R.id.twitterOption -> {
                            activity?.startActivity(
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
}
