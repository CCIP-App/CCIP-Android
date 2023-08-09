package app.opass.ccip.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import app.opass.ccip.model.Session
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

object AlarmUtil {
    fun setSessionAlarm(context: Context, session: Session) {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SET_ALARM) == PackageManager.PERMISSION_GRANTED) {
                    setExactAlarm(alarmManager, calendar, pendingIntent)
                } else {
                    requestExactAlarmPermission(context)
                    // Toast.makeText(context, "請在設定中開啟鬧鐘權限以設定活動提醒", Toast.LENGTH_LONG).show()
                }
            } else {
                setExactAlarm(alarmManager, calendar, pendingIntent)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setExactAlarm(alarmManager: AlarmManager, calendar: Calendar, pendingIntent: PendingIntent) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis - 10 * 60 * 1000,
            pendingIntent
        )
    }

    private fun requestExactAlarmPermission(context: Context) {
        val uri = Uri.parse("package:" + context.packageName)
        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri)
        } else {
            intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
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
}
