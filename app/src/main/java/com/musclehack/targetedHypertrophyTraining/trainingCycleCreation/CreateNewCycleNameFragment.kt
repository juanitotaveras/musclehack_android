package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.FragmentCreateNewCycleNameBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

private const val CREATE_REQUEST = 1337

class CreateNewCycleNameFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CycleCreatorViewModel
    private lateinit var viewDataBinding: FragmentCreateNewCycleNameBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = viewModelFactory.create(CycleCreatorViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding =
            FragmentCreateNewCycleNameBinding.inflate(inflater, container, false).apply {

            }
//        cycleNameTextBox!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) hideSoftKeyboard(v!!)
//        }

        // Make layout clickable
        viewDataBinding.createNewCycleLinearLayout.setOnClickListener {
            hideSoftKeyboard(it)
        }
        when (viewModel.templateSelection) {
            CycleTemplates0.FiveDaySplit -> {
                val textBoxText = "THT 5-Day Split"
                viewDataBinding.cycleNameTextBox.setText(textBoxText)
            }

            CycleTemplates0.ThreeDaySplit -> {
                val textBoxText = "THT 3-Day Split"
                viewDataBinding.cycleNameTextBox.setText(textBoxText)
            }

            CycleTemplates0.ThreeDayFull -> {
                val textBoxText = "THT 3-Day Full Body"
                viewDataBinding.cycleNameTextBox.setText(textBoxText)

            }

            else -> {
                val textBoxText = "Create Your Own"
            }
        }

        viewDataBinding.cycleNameTextBox.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) hideSoftKeyboard(v!!)
            }
        return viewDataBinding.root
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createCycleButton) {
            /* Get text from textbox. */
            val cycleName = viewDataBinding.cycleNameTextBox.text.toString()
            viewModel.newCycleName = cycleName
            val i = Intent(activity, CreatingCycleActivity::class.java)
            startActivityForResult(i, CREATE_REQUEST)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == CREATE_REQUEST && resultCode == Activity.RESULT_OK) {
            val i = Intent(activity, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_create_new_cycle_last, menu)
    }
}
