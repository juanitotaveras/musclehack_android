package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentLogPageBinding
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_DAY
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Author: Juanito Taveras
 * Created: 11/25/17
 * Modified: 11/25/17 12/26/17 1/13/18 3/10/18
 */

class LogPageFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val trainingViewModel by activityViewModels<TrainingViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: FragmentLogPageBinding
    private lateinit var adapter: LogsAdapter2

    companion object {
        internal fun newInstance(day: Int): LogPageFragment {
            val frag = LogPageFragment()
            val args = Bundle()
            args.putInt(KEY_DAY, day)
            frag.arguments = args
            return frag
        }
    }

    internal var activity: androidx.fragment.app.FragmentActivity? = null
    private val day: Int?
        get() = arguments?.getInt(KEY_DAY, -1)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentLogPageBinding.inflate(inflater, container, false).apply {
            trainingviewmodel = trainingViewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        setupListAdapter()
        setupLoadingAnimation()
    }

    private fun setupLoadingAnimation() {
        trainingViewModel.showLoadingAnimationForList.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { shouldShowLoading ->
                if (shouldShowLoading) {
                    viewDataBinding.logsList.visibility = View.INVISIBLE
                } else {
                    viewDataBinding.logsList.visibility = View.VISIBLE
                }
            })
    }

    private fun setupListAdapter() {
        val tviewModel = viewDataBinding.trainingviewmodel
        if (tviewModel != null && day != null) {
            val layoutManager = LinearLayoutManager(context)
            viewDataBinding.logsList.layoutManager = layoutManager
            adapter = LogsAdapter2(tviewModel, day!!)
            viewDataBinding.logsList.adapter = adapter
            // set start position
            if (trainingViewModel.getScrollPosition() > -10) {
                val offset = trainingViewModel.getScrollPosition()
//                layoutManager.scrollToPositionWithOffset(3, 0)
            }
            viewDataBinding.logsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    trainingViewModel.onRequestDefocus()
                    day?.let { d ->
                        val offset = recyclerView.computeVerticalScrollOffset()
                        trainingViewModel.setScrollPosition(offset, d)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // TODO: Put the scroll position here
                }
            })

            // observe for changes
            trainingViewModel.notifySetNumChangedEvent.observe(
                viewLifecycleOwner,
                Observer { event ->
                    event.peekContent()?.let { setNum ->
                        adapter.notifyItemChanged(setNum)
                    }
                })
        } else {
            // log here
        }
    }

    fun notifyCycleCompleted() {
        val toast = Toast.makeText(
            activity,
            R.string.cycle_completion_message,
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.BOTTOM, 0, 150)
        toast.show()
    }
}

