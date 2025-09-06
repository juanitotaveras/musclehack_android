package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.musclehack.targetedHypertrophyTraining.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class CreateNewCycleDurationFragment : DaggerFragment() {
    private lateinit var cycleDurationTextBox: EditText

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
            R.layout.fragment_create_new_cycle_duration,
            container, false
        )
        cycleDurationTextBox = result.findViewById<EditText>(R.id.cycleDurationTextBox)

        cycleDurationTextBox.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideSoftKeyboard(v!!)
        }

        // Make layout clickable
        result.findViewById<LinearLayout>(R.id.createNewCycleLinearLayout).setOnClickListener({}

        )

        return result
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create_new_cycle_intermediate, menu)
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createNewCycleNext) {
            /* Get text from textbox. */
            val cycleDuration = Integer.parseInt(cycleDurationTextBox.text.toString())
            viewModel.newCycleNumWeeks = cycleDuration
            val action =
                CreateNewCycleDurationFragmentDirections.actionCreateNewCycleDurationFragmentToCreateNewCycleNameFragment()
            findNavController().navigate(action)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


}
