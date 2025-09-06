package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_REST_NOTIFICATIONS

/** This notification ID must be different than our "foreground service"
 * notification ID. */
private const val NOTIFICATION_ID = 12

class NotificationHelper(
    val appContext: Context, private val exerciseName: String?,
    private val prefs: SharedPreferences
) {
    fun showRestTimeRemainingNotification(remainingRestTime: Int) {
        // only execute if user has this option on
        val notificationPref = prefs.getBoolean(PREF_REST_NOTIFICATIONS, true)
        if (notificationPref && remainingRestTime.rem(5) == 0) {
            val channelID = appContext.getString(R.string.default_notification_channel_id)
            val pendingIntent = TimerService.createPendingIntent(appContext)

            // TODO: Make string resource
            val notificationTitle = "Coming up: $exerciseName"
            val mBuilder = NotificationCompat.Builder(appContext, channelID)
                .setContentTitle(notificationTitle)
                .setContentText(
                    "You still need to rest ${
                        TimerService.secondsToMinutesFormat(
                            remainingRestTime
                        )
                    }"
                )
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_mh_icon_foreground)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)

            val mgr: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            mgr.notify(NOTIFICATION_ID, mBuilder.build()) // Message notification ID
        }
    }

    fun showRestTimeCompletedNotification(exerciseName: String?) {
        if (!prefs.getBoolean(PREF_REST_NOTIFICATIONS, true))
            return
        // only execute if user has this option on
        val notificationPref = prefs.getBoolean(PREF_REST_NOTIFICATIONS, true)

        if (notificationPref) {
            val channelID = appContext.getString(R.string.default_notification_channel_id)
            val pendingIntent = TimerService.createPendingIntent(appContext)

            val s = appContext.getString(R.string.rest_time_notification_head)
            val notificationTitle = String.format(s, exerciseName)
            val mBuilder = NotificationCompat.Builder(appContext, channelID)
                .setContentTitle(notificationTitle)
                .setContentText(appContext.getString(R.string.rest_time_up_notification_body))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_mh_icon_foreground)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)

            val mgr: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            // Make this one replace our foreground service notification.
            mgr.notify(NOTIFICATION_ID - 1, mBuilder.build())
        }
    }
}