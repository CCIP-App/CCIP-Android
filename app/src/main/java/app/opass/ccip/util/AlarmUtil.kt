package app.opass.ccip.util

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import app.opass.ccip.R
import app.opass.ccip.model.Session
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

object AlarmUtil {

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSessionAlarm(context: Context, session: Session) {
        if (!isNotificationEnabled(context)) {
            showNotificationPermissionDialog(context)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarm(context)) {
            val uri = Uri.parse("package:" + context.packageName)
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri))
            return
        }

        try {
            val date = ISO8601Utils.parse(session.start, ParsePosition(0))
            val calendar = Calendar.getInstance()
            calendar.time = date

            val intent = Intent(context, SessionAlarmReceiver::class.java)
            intent.action = session.id
            intent.putExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID, session.id)

            val pendingIntent = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis - 10 * 60 * 1000,
                pendingIntent
            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    fun cancelSessionAlarm(context: Context, session: Session) {
        val intent = Intent(context, SessionDetailActivity::class.java)
        intent.action = session.id

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(
            PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        )
    }

    private fun isNotificationEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun canScheduleExactAlarm(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermission(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS

        // for Android 5-7
        intent.putExtra("app_package", context.packageName)
        intent.putExtra("app_uid", context.applicationInfo.uid)

        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)

        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(R.string.on_login_request_notification_permission) // 使用您提供的資源 ID
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestNotificationPermission(context)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }
}
