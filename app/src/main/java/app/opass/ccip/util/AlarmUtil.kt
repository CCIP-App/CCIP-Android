package app.opass.ccip.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
                when {
                    alarmManager.canScheduleExactAlarms() -> {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis - 10 * 60 * 1000,
                            pendingIntent
                        )
                    }
                    else -> {
                        val uri = Uri.parse("package:" + context.packageName)
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri))
                    }
                }
            }
            else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis - 10 * 60 * 1000,
                    pendingIntent
                )
            }
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
}
