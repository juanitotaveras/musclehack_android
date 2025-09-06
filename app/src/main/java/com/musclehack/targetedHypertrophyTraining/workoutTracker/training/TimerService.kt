package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.TimerReceiver
import com.musclehack.targetedHypertrophyTraining.utilities.*
import dagger.android.DaggerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject


/**
 * Created by juanito on 12/27/2017.
 * Modified: 1/24/2018
 */

private const val NOTIFICATION_ID = 12
private const val TAG = "TimerService"

class TimerService : DaggerService() {
    private var restTime: Int = 0
    private var clickTime: Long = 0
    private var timerTask: TimerTask? = null
    private var exerciseName: String? = null
    private var context: Context = this
    private var remainingRestTime: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var appContext: Context

    companion object {
        // Converts total seconds to format M:SS
        fun secondsToMinutesFormat(seconds: Int): String {
            val formattedMinutes = "${seconds / 60}"
            var formattedSeconds = "${seconds % 60}"
            if (formattedSeconds.length < 2) {
                formattedSeconds = "0$formattedSeconds"
            }
            return "$formattedMinutes:$formattedSeconds"
        }

        /** Intent that is fired when user taps on notification. */
        fun createPendingIntent(appContext: Context): PendingIntent {
            val i = Intent(appContext, MainActivity::class.java)
            i.putExtra(EXTRA_IS_TIMER_INTENT, true)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntentFlags =
                if (VERSION.SDK_INT >= 23) PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_CANCEL_CURRENT
            return PendingIntent.getActivity(
                appContext, 87, i,
                pendingIntentFlags
            )
        }
    }

    private fun createAlarmPendingIntent(): PendingIntent {
        val i = Intent(context, TimerFinishedReceiver::class.java)
        val pendingIntentFlags =
            if (VERSION.SDK_INT >= 23) PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_CANCEL_CURRENT
        return PendingIntent.getBroadcast(
            this, 629, i,
            pendingIntentFlags
        )
    }

    /**
     * Schedules an exact alarm for rest timer completion.
     * Uses exact alarms when permission is available, falls back to regular alarms otherwise.
     */
    private fun scheduleRestTimerAlarm() {
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmPermissionHelper = AlarmPermissionHelper.create(this)
        val triggerTime = SystemClock.elapsedRealtime() + (restTime * 1000) + 1000

        if (alarmPermissionHelper.canScheduleExactAlarms()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                mgr,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                createAlarmPendingIntent()
            )
            Log.i(TAG, "Exact alarm scheduled for rest timer completion")
        } else {
            // Fallback to regular alarm if exact alarm permission is not granted
            mgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                createAlarmPendingIntent()
            )
            Log.w(TAG, "Using regular alarm - exact alarm permission not granted")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val eventType = intent?.getStringExtra(KEY_TIMER_EVENT_TYPE)
        if (eventType != null) {
            when (TimerCommandType.valueOf(eventType)) {
                TimerCommandType.START -> {
                    if (Build.MANUFACTURER.equals("sony") && android.provider.Settings.Secure.getInt(
                            context.contentResolver,
                            "somc.stamina_mode",
                            0
                        ) > 0
                    ) {
                        // show warning
                        Log.w(TAG, "Stamina Mode is ON. Beware.")
                    }
                    restTime = intent.getIntExtra(KEY_REST_TIME, -1)
                    clickTime = intent.getLongExtra(KEY_CLICK_TIME, -1)
                    exerciseName = intent.getStringExtra(KEY_UPCOMING_EXERCISE)
                    startForeground(NOTIFICATION_ID, createStartTimerNotification())
                    Log.i(TAG, "startForeground was just called in our service.")
                    if (timerTask != null) {
                        timerTask?.cancel(true)
                        timerTask = null
                    }
                    timerTask = TimerTask(this)
                    timerTask?.execute()
                    scheduleRestTimerAlarm()
                }

                TimerCommandType.STOP -> {
                    timerTask?.cancel(true)
                    stopForeground(true)
                    timerTask = null
                    mediaPlayer?.release()
                    val i = Intent(TimerReceiver.TIMER_CHANGE_ACTION)
                    i.putExtra(TimerReceiver.REMAINING_TIME_KEY, 0)
                    i.putExtra(TimerReceiver.EVENT_TYPE_KEY, TimerEventType.STOP.toString())
                    broadcast(i)
                    val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr.cancel(createAlarmPendingIntent())
                }

                TimerCommandType.PAUSE -> {
                    timerTask?.cancel(true)
                    timerTask = null
                    mediaPlayer?.release()
                    val i = Intent(TimerReceiver.TIMER_CHANGE_ACTION)
                    i.putExtra(TimerReceiver.REMAINING_TIME_KEY, 0)
                    i.putExtra(TimerReceiver.EVENT_TYPE_KEY, TimerEventType.PAUSE.toString())
                    broadcast(i)
                    val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr.cancel(createAlarmPendingIntent())
                }

                TimerCommandType.RESUME -> {
                    if (timerTask != null) {
                        timerTask?.cancel(true)
                        timerTask = null
                    }
                    restTime = remainingRestTime
                    clickTime = intent.getLongExtra(KEY_CLICK_TIME, -1)
                    startForeground(NOTIFICATION_ID, createStartTimerNotification())
                    timerTask = TimerTask(this)
                    timerTask?.execute()
                    scheduleRestTimerAlarm()
                }
            }
        }
        return Service.START_STICKY
    }

    private fun createStartTimerNotification(): Notification {
        val channelID = getString(R.string.default_notification_channel_id)
        //val notificationID = (System.currentTimeMillis() % 10000).toInt()
        val pendingIntent = createPendingIntent(context)
        val notificationTitle = "Coming up: $exerciseName"
        val mBuilder = NotificationCompat.Builder(this, channelID)
            .setContentTitle(notificationTitle)
            .setContentText("You still need to rest ${secondsToMinutesFormat(restTime)}")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_mh_icon_foreground)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
        return mBuilder.build()
    }


    override fun onDestroy() {
        Log.i(TAG, "onDestroy() called")
        timerTask?.let {
            it.cancel(true)
            timerTask = null
            Log.i(TAG, "just cancelled TimerService")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun broadcast(intent: Intent) {
        val mgr = LocalBroadcastManager.getInstance(context)
        mgr.sendBroadcast(intent)
    }

    /** This task updates our notification. */
    class TimerTask(timerService: TimerService) : AsyncTask<Void?, Long?, Void?>() {
        var finished = false
        private val serviceRef = WeakReference(timerService)

        override fun doInBackground(vararg unused: Void?): Void? {
            val ref = serviceRef.get()
            if (ref != null) {
                var timeDiffArg: Long
                while (!isCancelled && !finished) {
                    timeDiffArg = Date().time - ref.clickTime
                    publishProgress(timeDiffArg)
                    SystemClock.sleep(999)
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg timeDiffArg: Long?) {
            val ref = serviceRef.get()
            if (ref != null) {
                val timeDiff = (timeDiffArg[0]?.div(1000))!!.toInt()
                if (!isCancelled) {
                    ref.remainingRestTime = ref.restTime - timeDiff
                    Log.i(TAG, "remainingRestTime: " + ref.remainingRestTime)
                    val i = Intent(TimerReceiver.TIMER_CHANGE_ACTION)
                    i.putExtra(TimerReceiver.REMAINING_TIME_KEY, ref.remainingRestTime)
                    i.putExtra(TimerReceiver.EVENT_TYPE_KEY, TimerEventType.TICK.toString())
                    ref.broadcast(i)
                    if (ref.preferences.getBoolean(PREF_REST_NOTIFICATIONS, true)) {
                        val notificationHelper =
                            NotificationHelper(ref.context, ref.exerciseName, ref.preferences)
                        notificationHelper.showRestTimeRemainingNotification(ref.remainingRestTime)
                    }
                    if (timeDiff >= ref.restTime)
                        finished = true
                }
            } else finished = true

        }

        override fun onPostExecute(unused: Void?) {
            val ref = serviceRef.get()
            if (ref != null) {
                val notificationHelper =
                    NotificationHelper(ref.context, ref.exerciseName, ref.preferences)
                notificationHelper.showRestTimeCompletedNotification(ref.exerciseName)
                ref.timerTask = null
                val i = Intent(TimerReceiver.TIMER_CHANGE_ACTION)
                i.putExtra(TimerReceiver.REMAINING_TIME_KEY, 0)
                i.putExtra(TimerReceiver.EVENT_TYPE_KEY, TimerEventType.FINISH.toString())
                ref.broadcast(i)
                GlobalScope.launch(Dispatchers.Default) {
                    clearForegroundServiceAfterWait(ref)
                }
            }
        }

        private suspend fun clearForegroundServiceAfterWait(service: TimerService) {
            coroutineScope {
                SystemClock.sleep(2000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    service.stopForeground(true)
                } else {
                    service.stopSelf()
                }
            }
        }
    }
}
