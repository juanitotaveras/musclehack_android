/*
Author: Juanito Taveras
Created: 11/24/17
Modified: 11/24/17
 */

package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CreateNewCycleRepRangeFragment : DaggerFragment() {

    private lateinit var chooseRepRangeRadioGroup: RadioGroup

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CycleCreatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFactory.create(CycleCreatorViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val result = inflater.inflate(
            R.layout.fragment_create_new_cycle_rep_range, container,
            false
        )
        chooseRepRangeRadioGroup = result.findViewById(R.id.chooseRepRangeRadioGroup)
        val radio6To8 = result.findViewById<RadioButton>(R.id.radio_6_to_8_reps)
        val radio8To12 = result.findViewById<RadioButton>(R.id.radio_8_to_12_reps)
        radio8To12.isChecked = true
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createNewCycleNext) {
            /* Get text from textbox. */
            val repRangeSelectionID = chooseRepRangeRadioGroup.checkedRadioButtonId
            when (repRangeSelectionID) {
                R.id.radio_6_to_8_reps -> {
                    viewModel.lowerReps = 6
                    viewModel.higherReps = 8
                }

                R.id.radio_8_to_12_reps -> {
                    viewModel.lowerReps = 8
                    viewModel.higherReps = 12
                }
            }
            val action =
                CreateNewCycleRepRangeFragmentDirections.actionCreateNewCycleRepRangeFragmentToCreateNewCycleDurationFragment()
            findNavController().navigate(action)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create_new_cycle_intermediate, menu)
    }
}
