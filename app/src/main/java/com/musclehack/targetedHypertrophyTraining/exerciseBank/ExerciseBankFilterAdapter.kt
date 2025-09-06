package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.databinding.ExerciseBankItemBinding

/**
 * Created by juanito on 3/24//2018.
 */
class ExerciseBankFilterAdapter(val viewModel: ExerciseBankViewModel) :
    ListAdapter<ExerciseBankItem, ExerciseBankFilterAdapter.ViewHolder>(ExerciseBankFilterCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder.from(parent, viewModel)
        viewHolder.binding.bankItemContainer.setOnClickListener {
            //                /* Highlight exercise and have check on upper right corner */
//                // Only perform onClick if we're not in the ExerciseBank Tab
            val position = viewHolder.adapterPosition
            viewModel.onFilteredExerciseClicked(position)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, viewModel)
    }

    class ViewHolder private constructor(val binding: ExerciseBankItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExerciseBankItem, viewModel: ExerciseBankViewModel) {
            viewModel.getFilteredItems().value?.let { items ->
                binding.exerciseBankListItem.text = items[adapterPosition].exerciseName
                binding.deleteCustomExercise.visibility = View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: ExerciseBankViewModel): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ExerciseBankItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ExerciseBankFilterCallback : DiffUtil.ItemCallback<ExerciseBankItem>() {
    override fun areItemsTheSame(oldItem: ExerciseBankItem, newItem: ExerciseBankItem): Boolean {
        return oldItem.exerciseName == newItem.exerciseName
    }

    override fun areContentsTheSame(oldItem: ExerciseBankItem, newItem: ExerciseBankItem): Boolean {
        return oldItem.exerciseName == newItem.exerciseName
    }

}
