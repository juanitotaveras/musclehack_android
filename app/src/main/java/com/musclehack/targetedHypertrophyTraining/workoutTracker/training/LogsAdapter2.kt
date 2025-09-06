package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.LogCardBinding

class LogsAdapter2(private val viewModel: TrainingViewModel, private val day: Int) :
    ListAdapter<LogCardModel, LogsAdapter2.ViewHolder>(LogsDiffCallback()) {
    class ViewHolder private constructor(val binding: LogCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: TrainingViewModel, item: LogCardModel, day: Int, position: Int) {
            binding.logcardmodel = item
            binding.viewmodel = viewModel
            binding.position = position

            /** If this is our last exercise set */
            if (item.isLastSet(viewModel)) {
                binding.restTimerButton.text =
                    viewModel.appContext.getString(R.string.done_button_text)
            } else {
                /** This is not our last exercise set */
                binding.restTimerButton.text =
                    TimerService.secondsToMinutesFormat(seconds = item.exerciseSet.restTime)
            }

            if (item.prevSetData.prevNoteDay == null || item.prevSetData.prevNoteDay!! < 0) {
                /** No previous note is present. */
                clearPreviousNoteButton()
            } else {
                makePreviousNoteButtonVisible()
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: TrainingViewModel): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LogCardBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        private fun makePreviousNoteButtonVisible() {
            // configure prev note button
            binding.prevNoteButton.visibility = View.VISIBLE
            val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            lp.addRule(RelativeLayout.ALIGN_LEFT, binding.exerciseNameText.id)
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            lp.setMargins(-15, 0, 15, 0)
            binding.prevNoteButton.layoutParams = lp

            // put add note button to right
            val lp2 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            lp2.addRule(RelativeLayout.RIGHT_OF, binding.prevNoteButton.id)
            binding.noteButton.layoutParams = lp2
        }

        private fun clearPreviousNoteButton() {
            binding.prevNoteButton.visibility = View.GONE

            val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            lp.addRule(RelativeLayout.ALIGN_LEFT, binding.exerciseNameText.id)
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            lp.setMargins(-15, 0, 0, 0)
            binding.noteButton.layoutParams = lp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder.from(parent, viewModel)
        val binding = viewHolder.binding
        binding.currentWeightTextBox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val weightStr = p0.toString()
                if (binding.currentWeightTextBox.isFocused) {
                    binding.position?.let {
                        viewModel.onWeightTextChanged(weightStr, it, day)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.currentRepsTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val repsStr = p0.toString()
                if (binding.currentRepsTextBox.isFocused) {
                    binding.position?.let {
                        viewModel.onRepsTextChanged(repsStr, it, day)
                    }
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.currentWeightTextBox.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                viewModel.onEditTextFocused(hasFocus, viewHolder.adapterPosition)
            }
        binding.currentRepsTextBox.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                viewModel.onEditTextFocused(hasFocus, viewHolder.adapterPosition)
            }
        /** If this is our last exercise set */
        binding.restTimerButton.setOnClickListener {
            viewModel.onRequestDefocus()

            viewModel.getLogCard(viewHolder.adapterPosition)?.let { logCardModel ->
                if (logCardModel.isLastSet(viewModel)) {
                    // notify cycle completed
                    viewModel.saveTrainingTime()
                    viewModel.notifyCycleCompleted()
                } else {
                    viewModel.onStartTimerClicked(viewHolder.adapterPosition)
                }
            }
        }

        binding.optionsIcon.setOnClickListener {
            viewModel.onLogCardOptionsClicked(viewHolder.adapterPosition)
        }

        viewModel.requestDefocusEvent.observeForever { setNum ->
            if (setNum.peekContent() == viewHolder.adapterPosition) {
                binding.currentWeightTextBox.clearFocus()
                binding.currentRepsTextBox.clearFocus()
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, day, position)
    }
}

class LogsDiffCallback : DiffUtil.ItemCallback<LogCardModel>() {
    override fun areItemsTheSame(oldItem: LogCardModel, newItem: LogCardModel): Boolean {
        return oldItem.exerciseLog.setId == newItem.exerciseLog.setId
    }

    override fun areContentsTheSame(oldItem: LogCardModel, newItem: LogCardModel): Boolean {
        // ignore item being edited so it's not rebuilt.
        if (oldItem.isFocused) return true

        return oldItem.hasEqualContents(newItem)
    }
}

