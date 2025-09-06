package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musclehack.targetedHypertrophyTraining.databinding.BottomSheetTrackerHomeBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TrackerHomeBottomDialogFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel by activityViewModels<TrackerHomeViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: BottomSheetTrackerHomeBinding
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = BottomSheetTrackerHomeBinding.inflate(inflater, container, false).apply {
            // set vars here
            viewmodel = viewModel
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Note: EventObserver {} means no action is taken if event has already been observed!
        // TODO: Define a bottom dialog dismissed event.
        viewModel.dismissBottomSheetEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                dismiss()
            }
        })
    }
}