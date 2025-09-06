package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle

/** [BindingAdapter]s for [Cycle]s list.
 *
 */
@BindingAdapter("app:cycles")
fun setItems(listView: RecyclerView, cycles: List<Cycle>?) {
    cycles?.let {
        (listView.adapter as CyclesAdapter2).submitList(cycles)
    }
}
