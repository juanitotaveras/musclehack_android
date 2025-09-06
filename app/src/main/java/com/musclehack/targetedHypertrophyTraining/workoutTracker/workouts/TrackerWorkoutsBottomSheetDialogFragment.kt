package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musclehack.targetedHypertrophyTraining.databinding.BottomSheetTrackerWorkoutsBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TrackerWorkoutsBottomSheetDialogFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel by activityViewModels<TrackerWorkoutsViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: BottomSheetTrackerWorkoutsBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding =
            BottomSheetTrackerWorkoutsBinding.inflate(inflater, container, false).apply {
                viewmodel = viewModel
                workout = viewModel.currentlySelectedWorkout.value
            }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.dismissBottomSheetEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                dismiss()
            }
        })
    }

    enum class TrackerWorkoutsBottomSheetOption {
        RENAME,
        DELETE
    }
}