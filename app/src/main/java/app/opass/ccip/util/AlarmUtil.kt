package app.opass.ccip.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
            intent.action = session.hashCode().toString()
            intent.putExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID, session.id)

            val pendingIntent = PendingIntent
                .getBroadcast(context, session.hashCode(), intent, 0)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis - 10 * 60 * 1000,
                    pendingIntent
                )
            } else {
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
        intent.putExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID, session.id)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(
            PendingIntent
                .getBroadcast(context, session.hashCode(), intent, 0)
        )
    }
}
