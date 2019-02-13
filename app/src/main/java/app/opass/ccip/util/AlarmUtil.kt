package app.opass.ccip.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.opass.ccip.activity.SubmissionDetailActivity
import app.opass.ccip.model.Submission
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

object AlarmUtil {
    fun setSubmissionAlarm(context: Context, submission: Submission) {
        try {
            val date = ISO8601Utils.parse(submission.start, ParsePosition(0))
            val calendar = Calendar.getInstance()
            calendar.time = date

            val intent = Intent(context, SubmissionAlarmReceiver::class.java)
            intent.action = submission.hashCode().toString()
            intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission))

            val pendingIntent = PendingIntent
                .getBroadcast(context, submission.hashCode(), intent, 0)

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

    fun cancelSubmissionAlarm(context: Context, submission: Submission) {
        val intent = Intent(context, SubmissionDetailActivity::class.java)
        intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission))

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(
            PendingIntent
                .getBroadcast(context, submission.hashCode(), intent, 0)
        )
    }
}
