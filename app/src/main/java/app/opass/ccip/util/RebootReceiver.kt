package app.opass.ccip.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val starredSessions = ScheduleUtil.getStarredSessions(context)
        for (session in starredSessions) {
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
