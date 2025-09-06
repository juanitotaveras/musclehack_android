package com.musclehack.targetedHypertrophyTraining

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.preference.PreferenceManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.utilities.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class SplashActivity : AppCompatActivity() {

    private val prefs = AtomicReference<SharedPreferences>()

    @SuppressLint("NewApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val videoView: VideoView = findViewById(R.id.videoView)
        prefs.set(PreferenceManager.getDefaultSharedPreferences(this))
        //    SharedPreferences myPrefs = prefs;
        val myPrefs = prefs.get()


        /* Configure notification channel */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelID = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val mgr = getSystemService(NotificationManager::class.java) as NotificationManager
            mgr.createNotificationChannel(
                NotificationChannel(
                    channelID, channelName,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        if (!myPrefs.getBoolean(PREF_USER_RATED, false)) {
            val startsSinceAsk = myPrefs.getInt(PREF_STARTS_SINCE_ASK_RATE, -1)
            myPrefs.edit().putInt(PREF_STARTS_SINCE_ASK_RATE, startsSinceAsk + 1).apply()
        }


        if (!myPrefs.getBoolean(PREF_LONG_SPLASH, true)) {
            startNextActivity()
        } else {
            /* This is the first time we boot the app. */
            InitialConfigurationThread(this).start()


            /*
            val video: Uri =
                    Uri.parse(
                            "android.resource://$packageName/${R.raw.splash_vid2}"
                    )
            Log.e("x", "video: $video")
            videoView.setVideoURI(video)

            videoView.setOnCompletionListener {

                startNextActivity()
            }

            //videoView.setOnErrorListener(mOnErrorListener)

            videoView.start()
            */
            startNextActivity()
        }

    }

    private fun startNextActivity() {
        if (isFinishing)
            return
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private inner class InitialConfigurationThread(val ctxt: Context) : Thread() {
        @SuppressLint("ApplySharedPref")
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            val myPrefs = prefs.get()

            /* Set longSplash to false. */
            if (!myPrefs.contains(PREF_INSTALL_DATE))
                myPrefs.edit().putLong(PREF_INSTALL_DATE, Date().time).apply()

            myPrefs.edit().putBoolean(PREF_LONG_SPLASH, false)
                .putString(PREF_CURRENT_TAB, TAB_TRACKER_HOME)
                .putString(PREF_CURRENT_TRACKER_SCREEN, FRAG_TRACKER_HOME)
                .putBoolean(PREF_SHOW_DELETE_SET_WARNING, true)
                .putString(PREF_REST_TIME_DEFAULT_SOUND, "2").commit()

            prefs.set(myPrefs)
        }
    }

}