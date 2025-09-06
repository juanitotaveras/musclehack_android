package com.musclehack.targetedHypertrophyTraining.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_TIME_DEFAULT_SOUND

class PreferenceDisplay : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    /* This is the selection of default sounds preference. */
    private val restTimeSoundPref: ListPreference?
        get() = preferenceScreen.findPreference(PREF_REST_TIME_DEFAULT_SOUND) as ListPreference?

    override fun onResume() {
        super.onResume()

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        val prefs = preferenceScreen.sharedPreferences

        restTimeSoundPref?.summary = if ("${restTimeSoundPref?.entry}" != "None")
            "You will hear \"${restTimeSoundPref?.entry}\" when your rest time is up."
        else
            "No sound will be played."
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_display)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        // Set new summary, when a preference value changes

        when (key) {
            PREF_REST_TIME_DEFAULT_SOUND -> {
                if ("${restTimeSoundPref?.entry}" == "None") {
                    restTimeSoundPref?.summary = "No sound will be played."
                    // Need to also remove path to ringtonePref
                } else {
                    restTimeSoundPref?.summary =
                        "You will hear \"${restTimeSoundPref?.entry}\" when your rest time is up."
                }
            }
        }
    }
}
