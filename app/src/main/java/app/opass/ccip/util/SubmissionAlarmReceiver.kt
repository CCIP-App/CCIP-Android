package app.opass.ccip.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.opass.ccip.R
import app.opass.ccip.activity.SubmissionDetailActivity
import app.opass.ccip.model.Submission

class SubmissionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val submission = JsonUtil
            .fromJson(
                intent.getStringExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM),
                Submission::class.java
            )

        intent.setClass(context, SubmissionDetailActivity::class.java)
        intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notificationContent = String
            .format(
                context.getString(R.string.notification_submission_start),
                submission.getSubmissionDetail(context).subject,
                submission.room
            )

        val manager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_ID = "submission_bookmark"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.bookmark_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bookmark_black_24dp)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(notificationContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationContent))
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(Notification.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_ALARM)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
