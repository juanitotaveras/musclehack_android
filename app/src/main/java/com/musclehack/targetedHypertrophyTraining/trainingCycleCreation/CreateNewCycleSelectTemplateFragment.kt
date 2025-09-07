package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

private const val CREATE_REQUEST = 1337

class CreateNewCycleSelectTemplateFragment : DaggerFragment() {
    private lateinit var templateSelectionRadioGroup: RadioGroup

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CycleCreatorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val result = inflater.inflate(
            R.layout.fragment_create_new_cycle_template_selection,
            container, false
        )
        templateSelectionRadioGroup = result.findViewById(R.id.templateSelectionRadioGroup)
        return result
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.clearFab()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createNewCycleNext) {
            when (templateSelectionRadioGroup.checkedRadioButtonId) {
                R.id.fiveDaySplitRadioButton ->
                    viewModel.templateSelection = CycleTemplates0.FiveDaySplit

                R.id.threeDaySplitRadioButton ->
                    viewModel.templateSelection = CycleTemplates0.ThreeDaySplit

                R.id.threeDayFullBodyRadioButton ->
                    viewModel.templateSelection = CycleTemplates0.ThreeDayFull

                else ->
                    viewModel.templateSelection = CycleTemplates0.Blank
            }

            if (viewModel.templateSelection == CycleTemplates0.Blank) {
                val action = CreateNewCycleSelectTemplateFragmentDirections
                    .actionCreateNewCycleSelectTemplateFragmentToCreateNewCycleDurationFragment()
                findNavController().navigate(action)
            } else {
                val action = CreateNewCycleSelectTemplateFragmentDirections
                    .actionCreateNewCycleSelectTemplateFragmentToCreateNewCycleRepRangeFragment()
                findNavController().navigate(action)
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == CREATE_REQUEST && resultCode == Activity.RESULT_OK)
            activity?.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFactory.create(CycleCreatorViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_create_new_cycle_intermediate, menu)
    }
}
