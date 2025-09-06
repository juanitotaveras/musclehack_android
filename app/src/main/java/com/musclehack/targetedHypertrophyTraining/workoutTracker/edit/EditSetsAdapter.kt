package com.musclehack.targetedHypertrophyTraining.workoutTracker.edit

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.databinding.SetsListItemBinding
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.ExerciseSet

class EditSetsAdapter(private val viewModel: EditSetsViewModel) :
    ListAdapter<ExerciseSet, EditSetsAdapter.ViewHolder>(SetsDiffCallback()) {

    class ViewHolder private constructor(val binding: SetsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(viewModel: EditSetsViewModel, item: ExerciseSet, position: Int) {
            binding.viewmodel = viewModel
            binding.exerciseSet = item
            binding.position = position
            binding.optionsIcon.visibility =
                if (viewModel.editSetsSortMode.value!!) View.VISIBLE else View.INVISIBLE
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SetsListItemBinding.inflate(layoutInflater, parent, false)
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

class SetsDiffCallback : DiffUtil.ItemCallback<ExerciseSet>() {
    override fun areItemsTheSame(oldItem: ExerciseSet, newItem: ExerciseSet): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ExerciseSet, newItem: ExerciseSet): Boolean {
        return oldItem.id == newItem.id && oldItem.hasEqualContents(newItem)
    }
}