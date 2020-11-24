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
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity

class SessionAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "session_bookmark"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val session = PreferenceUtil.loadSchedule(context)?.sessions?.find {
            it.id == intent.getStringExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID)
        } ?: return

        val newIntent = Intent(context, SessionDetailActivity::class.java).apply {
            action = intent.action
            putExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID, session.id)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0)

        val notificationContent = String
            .format(
                context.getString(R.string.notification_session_start),
                session.getSessionDetail(context).title,
                session.room.getDetails(context).name
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
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
