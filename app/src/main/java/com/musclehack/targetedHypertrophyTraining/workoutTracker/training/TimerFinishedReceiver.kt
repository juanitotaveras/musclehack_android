package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.SystemClock
import android.os.Vibrator
import android.util.Log
import androidx.preference.PreferenceManager
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_IMPORT_SOUND_ON
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_TIME_DEFAULT_SOUND
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_TIME_IMPORT_SOUND
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_TIME_VIBRATION
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_TIME_VOLUME
import java.io.IOException

/** Receiver to sound and vibrate alarm when timer has finished. */
/** TimerReceiver cannot do this because it is only configured to run when app is running.*/
class TimerFinishedReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            executeVibration(context, prefs)

            playRestTimeSound(context, prefs)
        }
    }

    private fun executeVibration(appContext: Context, prefs: SharedPreferences) {
        if (prefs.getBoolean(PREF_REST_TIME_VIBRATION, true)) {
            val v = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            /* First element is delay amount, then alternates between vibrate and sleep. */
            val vibrationPattern = longArrayOf(0, 400, 400, 400, 400, 400)
            /* If URI is length of Ringtone, play one of the default selections. */
            v.vibrate(vibrationPattern, -1)
            Log.e("p", "vibrator: $v")
            Log.e("p", "has vibrator: ${v.hasVibrator()}")
            //            v.vibrate(VibrationEffect.createOneShot())
        }
    }

    private fun playRestTimeSound(appContext: Context, prefs: SharedPreferences) {

        val restTimeImportSoundSelection =
            prefs.getString(PREF_REST_TIME_IMPORT_SOUND, "") ?: ""
        val importSoundOn = prefs.getBoolean(PREF_IMPORT_SOUND_ON, false)
                && restTimeImportSoundSelection.isNotEmpty()

        if (importSoundOn) {
            playImportedSound(appContext, prefs, restTimeImportSoundSelection)
        } else {
            val restTimeDefaultSoundSelection =
                Integer.parseInt(prefs.getString(PREF_REST_TIME_DEFAULT_SOUND, "0") ?: "0")
            if (restTimeDefaultSoundSelection != 0) {
                playRestTimeDefaultSound(appContext, restTimeDefaultSoundSelection)
            }
        }
    }

    private fun playImportedSound(appContext: Context, prefs: SharedPreferences, uri: String) {
        val importSoundURI = Uri.parse(uri)
        mediaPlayer = MediaPlayer()// = MediaPlayer.create(context, resID);
        /* Play URI of selected sound here. */
        try {
            mediaPlayer?.let { player ->
                player.setDataSource(appContext, importSoundURI)
                val audioManager = appContext
                    .getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                    /*player.setAudioStreamType(AudioManager.STREAM_ALARM)*/
                    val vol = prefs.getFloat(PREF_REST_TIME_VOLUME, (1).toFloat())
                    player.setVolume(vol, vol)
                    player.prepare()
                    player.start()
                    PlayImportSoundTask(player).execute()
                }
            }
        } catch (e: IOException) {
//                    Log.e(javaClass.name, "IO Exception", e)
        }
    }

    /* Helper class to stop custom sound after 3 seconds. */
    class PlayImportSoundTask(private val mediaPlayer: MediaPlayer) :
        AsyncTask<Void?, Long?, Void?>() {
        /* TODO: Add custom length in settings. */
        override fun doInBackground(vararg unused: Void?): Void? {
            SystemClock.sleep(3000) // 3 seconds sleep
            return null
        }

        override fun onPostExecute(unused: Void?) {
            mediaPlayer.stop()
        }
    }

    private fun playRestTimeDefaultSound(appContext: Context, selection: Int) {
        var resID = 0
        when (selection) {
            1 -> resID = R.raw.robot_start_voice
            2 -> resID = R.raw.two_beeps
            3 -> resID = R.raw.five_beeps
            4 -> resID = R.raw.bell
        }
        mediaPlayer = MediaPlayer.create(appContext, resID)
        mediaPlayer?.let { player ->
            val vol = 1F//ref.preferences.getFloat(PREF_REST_TIME_VOLUME, (1).toFloat())
            player.setVolume(vol, vol)
            player.start()
        }
    }
}