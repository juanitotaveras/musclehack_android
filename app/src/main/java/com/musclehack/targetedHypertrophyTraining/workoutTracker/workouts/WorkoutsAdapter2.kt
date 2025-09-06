package com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.databinding.WorkoutListItemBinding
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Workout

class WorkoutsAdapter2(private val viewModel: TrackerWorkoutsViewModel) :
    ListAdapter<Workout, WorkoutsAdapter2.ViewHolder>(WorkoutsDiffCallback()) {
    class ViewHolder private constructor(val binding: WorkoutListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: TrackerWorkoutsViewModel, item: Workout, position: Int) {
            binding.viewmodel = viewModel
            binding.workout = item
            binding.position = position
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = WorkoutListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, position)
    }
}

class WorkoutsDiffCallback : DiffUtil.ItemCallback<Workout>() {
    override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
        return oldItem.areContentsEqual(newItem)
    }
}