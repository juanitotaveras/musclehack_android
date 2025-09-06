package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet

/**
 * [BindingAdapter]s for [EditSets]s list
 */
@BindingAdapter("app:exerciseSets")
fun setItems(listView: RecyclerView, exerciseSets: List<ExerciseSet>?) {
    if (exerciseSets != null) {
        (listView.adapter as EditSetsAdapter).submitList(exerciseSets)
    }
}

// We need isSortMode, totalSetCount, and current position
@BindingAdapter(value = ["app:viewModel", "app:position"])
fun setStyle(container: LinearLayout, viewModel: EditSetsViewModel, position: Int) {
    var containerColor = viewModel.getColor(R.color.white)
    val sortMode = viewModel.editSetsSortMode.value
    if (sortMode != null && sortMode && position == viewModel.justMovedPosition) {
        containerColor = viewModel.getColor(R.color.justMoved2)
    }
    container.setBackgroundColor(containerColor)
}
