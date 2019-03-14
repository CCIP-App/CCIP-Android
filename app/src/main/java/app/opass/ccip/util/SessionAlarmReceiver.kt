package app.opass.ccip.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.opass.ccip.R
import app.opass.ccip.activity.SessionDetailActivity
import app.opass.ccip.model.Session

class SessionAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "session_bookmark"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val session = JsonUtil
            .fromJson(
                intent.getStringExtra(SessionDetailActivity.INTENT_EXTRA_PROGRAM),
                Session::class.java
            )

        intent.setClass(context, SessionDetailActivity::class.java)
        intent.putExtra(SessionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(session))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notificationContent = String
            .format(
                context.getString(R.string.notification_session_start),
                session.getSessionDetail(context).title,
                session.room
            )

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
