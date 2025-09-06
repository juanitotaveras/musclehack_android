package com.musclehack.targetedHypertrophyTraining.blog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.databinding.DialogConfirmOpenNotificationLinkBinding
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class ConfirmOpenNotificationLinkDialogFragment : DaggerDialogFragment(),
    DialogInterface.OnCancelListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<BlogViewModel> { viewModelFactory }
    private lateinit var viewDataBinding: DialogConfirmOpenNotificationLinkBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewDataBinding =
            DialogConfirmOpenNotificationLinkBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(activity)
        val res = viewDataBinding.root.context
        val dialogHeader = res.getString(R.string.open_link_confirmation)
        return builder.setTitle(dialogHeader).setView(viewDataBinding.root)
            .setPositiveButton(android.R.string.yes) { _: DialogInterface, i: Int ->
                viewModel.onConfirmOpenNotificationLinkClicked()
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, i: Int ->
                viewModel.onCancelOpenNotificationLinkClicked()
            }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onCancelOpenNotificationLinkClicked()
        super.onDismiss(dialog)
    }
}