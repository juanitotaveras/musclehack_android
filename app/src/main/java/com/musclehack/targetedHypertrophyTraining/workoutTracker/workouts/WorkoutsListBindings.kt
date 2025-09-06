package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout

/** [BindingAdapter]s for [Workout]s list.
 *
 */
@BindingAdapter("app:workouts")
fun setItems(listView: RecyclerView, workouts: List<Workout>?) {
    workouts?.let {
        (listView.adapter as WorkoutsAdapter2).submitList(it)
    }
}

// We need isSortMode, totalSetCount, and current position
@BindingAdapter(value = ["app:viewModel", "app:position"])
fun setStyle(container: LinearLayout, viewModel: TrackerWorkoutsViewModel, position: Int) {
    var containerColor = viewModel.getColor(R.color.white)
    val sortMode = viewModel.trackerWorkoutsSortMode.value
    if (sortMode != null && sortMode && position == viewModel.justMovedPosition) {
        containerColor = viewModel.getColor(R.color.justMoved2)
    }
    container.setBackgroundColor(containerColor)
}