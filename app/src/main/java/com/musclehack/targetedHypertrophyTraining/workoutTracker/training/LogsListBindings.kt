package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musclehack.targetedHypertrophyTraining.R

/** Binding adapter for logs list*/

@BindingAdapter("app:exerciseLogs")
fun setItems(listView: RecyclerView, logCardModels: List<LogCardModel>?) {
    if (logCardModels != null) (listView.adapter as LogsAdapter2).submitList(logCardModels)
}

@BindingAdapter(value = ["app:logcardmodel", "app:viewModel"])
fun setStyleForLayout(
    layout: LinearLayout,
    logCardModel: LogCardModel,
    viewModel: TrainingViewModel
) {
    if (logCardModel.exerciseSet.position == 0) {
        val paddingPixel = 50
        val density = viewModel.appContext.resources.displayMetrics.density
        val paddingDp = (paddingPixel * density).toInt()
        layout.setPadding(0, paddingDp, 0, 0)
    } else {
        layout.setPadding(0, 0, 0, 0)
    }
}

@BindingAdapter(value = ["app:item"])
fun setStyleForCardContainer(layout: CardView, item: LogCardModel) {
    var backgroundColor = ContextCompat.getColor(layout.context, R.color.white)
    if (item.exerciseLog.skip) {
        backgroundColor = ContextCompat.getColor(layout.context, R.color.justMoved2)
    } else if (item.exerciseLog.subName != null) {
        backgroundColor = ContextCompat.getColor(layout.context, R.color.paleYellow)
    }
    layout.setBackgroundColor(backgroundColor)
}

@BindingAdapter(value = ["app:logcardmodel", "app:viewModel"])
fun setStyleForNoteButton(
    button: AppCompatButton, logCardModel: LogCardModel,
    viewModel: TrainingViewModel
) {
    if (!logCardModel.exerciseLog.hasNote) {
        button.text = viewModel.appContext.getString(R.string.add_note_button)
    } else {
        button.text = viewModel.appContext.getString(R.string.edit_note_button)
    }
}
