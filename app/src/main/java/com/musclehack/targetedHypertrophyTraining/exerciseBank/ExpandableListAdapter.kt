package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.ExerciseBankItemBinding

// child data in format of header title, child title
// listDataHeader = categories
// listDatachild = exercises
class ExpandableListAdapter(private val viewModel: ExerciseBankViewModel) :
    BaseExpandableListAdapter() {

    // This listAdapter can only be created if defaultExercises is not null.
    private val defaultExercises: DefaultExercises = viewModel.getDefaultExercises().value!!

    override fun getChild(groupPosition: Int, childPosititon: Int): ExerciseBankItem {
        if (groupPosition == defaultExercises.categories.size) {
            val userExercise = viewModel.getUserExercises()[childPosititon]
            return ExerciseBankItem(userExercise.name, null)
        }
        return defaultExercises.exercises[defaultExercises.categories[groupPosition]]?.get(
            childPosititon
        )!!
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun onGroupExpanded(groupPosition: Int) {
        super.onGroupExpanded(groupPosition)
        viewModel.onExerciseCategoryClicked(groupPosition, true)
    }

    override fun onGroupCollapsed(groupPosition: Int) {
        super.onGroupCollapsed(groupPosition)
        viewModel.onExerciseCategoryClicked(groupPosition, false)
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView

        val childText = getChild(groupPosition, childPosition).exerciseName

        if (convertView == null) {
            val infalInflater = viewModel.getAppContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = ExerciseBankItemBinding.inflate(infalInflater)
            binding.viewmodel = viewModel
            convertView = binding.root
        }

//            ctxt ->  convertView?.setBackgroundColor(ContextCompat.getColor(viewModel.appContext, R.color.white))


        val txtListChild = convertView
            .findViewById<View>(R.id.exerciseBankListItem) as TextView

        txtListChild.text = childText
        val exerciseName = txtListChild.text.toString()
        val deleteIcon = convertView.findViewById<ImageButton>(R.id.deleteCustomExercise)

        setupDeleteButton(viewModel, deleteIcon, groupPosition, childPosition)
        convertView.setOnClickListener {
            viewModel.onExerciseClicked(groupPosition, childPosition)
        }
        return convertView
    }

    private fun setupDeleteButton(
        viewModel: ExerciseBankViewModel,
        deleteIcon: View?,
        groupPosition: Int,
        childPosition: Int
    ) {
        val vModel = viewModel as? ExerciseBankMainViewModel
        if (deleteIcon != null && vModel != null && vModel.isUserCreatedCategory(groupPosition)) {
            deleteIcon.visibility = View.VISIBLE
            deleteIcon.setOnClickListener { v ->
                vModel.onUserExerciseDeleteClicked(childPosition)
            }
        } else if (deleteIcon != null) {
            deleteIcon.visibility = View.GONE
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if (groupPosition == defaultExercises.categories.size) {
            // User category
            return viewModel.getUserExercises().size
        }
        val category = defaultExercises.exercises[defaultExercises.categories[groupPosition]]
        return category?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): String {
        if (groupPosition == defaultExercises.categories.size) {
            return "User Exercises"
        }
        return defaultExercises.categories[groupPosition]
    }


    override fun getGroupCount(): Int {
        val userExercisesGroup = if (
            viewModel.getUserExercises().isNotEmpty()) 1 else 0
        return defaultExercises.categories.size + userExercisesGroup
    }


    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var view = convertView
        val headerTitle = getGroup(groupPosition)
        if (view == null) {
            val infalInflater = viewModel.getAppContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = infalInflater.inflate(R.layout.exercise_bank_group, null)
        }

        val lblListHeader = view!!
            .findViewById<View>(R.id.exercise_bank_group) as TextView
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.text = headerTitle

        return view
    }

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
