package com.musclehack.targetedHypertrophyTraining.workoutTracker.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.CycleListItemBinding
import com.musclehack.targetedHypertrophyTraining.workoutTracker.entities.Cycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CyclesAdapter2(private val viewModel: TrackerHomeViewModel) :
    ListAdapter<Cycle, CyclesAdapter2.ViewHolder>(CycleDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder.from(parent)
        viewHolder.binding.optionsIcon.setOnClickListener {
            viewModel.onCycleOptionsButtonClicked(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    class ViewHolder private constructor(val binding: CycleListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: TrackerHomeViewModel, item: Cycle) {
            binding.viewmodel = viewModel
            binding.cycle = item
            val t = item.dateLastLogged
            if (t == 0L) {
                val ft = SimpleDateFormat("E MMM d y", Locale.US)
                var useStr = binding.root.context.getString(R.string.created)
                useStr += " " + ft.format(Date(item.dateCreated))
                binding.cycleListDate.text = useStr
            } else {
                val ft = SimpleDateFormat("E MMM d y", Locale.US)
                var useStr = binding.root.context.getString(R.string.last_used)
                useStr += " " + ft.format(Date(t))
                binding.cycleListDate.text = useStr
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CycleListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class CycleDiffCallback : DiffUtil.ItemCallback<Cycle>() {
    override fun areItemsTheSame(oldItem: Cycle, newItem: Cycle): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Cycle, newItem: Cycle): Boolean {
        return oldItem.areContentsEqual(newItem)
    }
}