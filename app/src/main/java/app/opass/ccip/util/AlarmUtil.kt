package app.opass.ccip.util

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import app.opass.ccip.R
import app.opass.ccip.model.Session
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

object AlarmUtil {

    private const val TAG = "AlarmUtil"

    fun setSessionAlarm(context: Context, session: Session) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // We don't have the permission, exit!
                Log.i(TAG, "Missing SCHEDULE_EXACT_ALARM permission!")
                if (context is Activity) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.perm_denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }

        // Try to schedule the Alarm assuming we have required permissions
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
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis - 10 * 60 * 1000,
                pendingIntent
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to schedule event!", exception)
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
