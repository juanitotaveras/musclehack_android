package com.musclehack.targetedHypertrophyTraining.utilities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Patterns
import android.view.View
import com.musclehack.targetedHypertrophyTraining.R

/**
 * Created by juanito on 1/16/2018.
 *
 */

class PleaseRateDialogFragment : androidx.fragment.app.DialogFragment(),
    DialogInterface.OnClickListener {
    private lateinit var form: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.please_rate_dialog, null)
        val builder = android.app.AlertDialog.Builder(activity)
//        val res = resources
        val dialogHeader = getString(R.string.rate_dialog_header)
        return builder.setTitle(dialogHeader).setView(form)
            .setPositiveButton(getString(R.string.will_rate), this)
            .setNeutralButton(
                getString(R.string.rate_later)
            ) { _, _ ->
                run {
                    PreferenceManager.getDefaultSharedPreferences(activity)
                        .edit().putInt(PREF_STARTS_SINCE_ASK_RATE, 0).apply()
                }
            }
            .setNegativeButton(getString(R.string.dont_ask_rate_again),
                { _, _ ->
                    run {
                        PreferenceManager.getDefaultSharedPreferences(activity)
                            .edit().putBoolean(PREF_USER_RATED, true).apply()
                    }
                })
            .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        // take user to play store
        PreferenceManager.getDefaultSharedPreferences(activity)
            .edit().putBoolean(PREF_USER_RATED, true).apply()
//        val i = Intent(activity, MainActivity::class.java)
        val link = resources.getString(R.string.google_play_link)
        // Only launch activity if URL is valid
        if (Patterns.WEB_URL.matcher(link).matches())
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

    }

}
