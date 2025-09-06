package com.musclehack.targetedHypertrophyTraining.workoutTracker.training

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.musclehack.targetedHypertrophyTraining.R

/**
 * Helper class to manage exact alarm permissions for rest timers.
 *
 * This class handles the proper flow for requesting SCHEDULE_EXACT_ALARM permission
 * which is required for accurate rest timer notifications during workouts.
 */
class AlarmPermissionHelper(private val context: Context) {

    /**
     * Checks if the app can schedule exact alarms.
     * For Android 12+ (API 31+), this requires explicit user permission.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // Before Android 12, exact alarms were allowed by default
            true
        }
    }

    /**
     * Requests permission to schedule exact alarms with user-friendly explanation.
     * Shows a dialog explaining why the permission is needed for rest timers.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestExactAlarmPermission() {
        AlertDialog.Builder(context)
            .setTitle(R.string.alarm_permission_title)
            .setMessage(R.string.alarm_permission_message)
            .setPositiveButton(R.string.alarm_permission_settings) { _, _ ->
                // Open the exact alarm settings page
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
            .setNegativeButton(R.string.alarm_permission_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Shows an educational dialog explaining why exact alarms are needed
     * before requesting permission. This helps with Google Play approval.
     */
    fun showPermissionEducationDialog(onProceed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.rest_timer_setup_title)
            .setMessage(R.string.rest_timer_setup_message)
            .setPositiveButton(R.string.rest_timer_setup_enable) { _, _ ->
                onProceed()
            }
            .setNegativeButton(R.string.rest_timer_setup_skip) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Checks permission and requests if needed. Call this before starting a timer.
     *
     * @param onPermissionGranted Callback when permission is available
     * @param onPermissionDenied Callback when permission is not available
     */
    fun ensureExactAlarmPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (canScheduleExactAlarms()) {
            onPermissionGranted()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            showPermissionEducationDialog {
                requestExactAlarmPermission()
            }
            onPermissionDenied()
        } else {
            onPermissionGranted()
        }
    }

    companion object {
        /**
         * Creates an instance of AlarmPermissionHelper
         */
        fun create(context: Context): AlarmPermissionHelper {
            return AlarmPermissionHelper(context)
        }
    }
}
