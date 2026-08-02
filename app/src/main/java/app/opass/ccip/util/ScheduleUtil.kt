package app.opass.ccip.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import app.opass.ccip.model.Session
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition

object ScheduleUtil {
    fun getStarredSessions(context: Context): List<Session> {
        val sessions = PreferenceUtil.loadSchedule(context)?.sessions ?: return emptyList()
        val starredIds = PreferenceUtil.loadStarredIds(context)
        return sessions.filter { starredIds.contains(it.id) }
    }

    fun rescheduleStarredSessionAlarms(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        for (session in getStarredSessions(context)) {
            try {
                val date = ISO8601Utils.parse(session.start, ParsePosition(0))
                if (System.currentTimeMillis() < date.time) {
                    AlarmUtil.setSessionAlarm(context, session)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }
}
