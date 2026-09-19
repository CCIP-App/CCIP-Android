package app.opass.ccip.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.opass.ccip.R
import app.opass.ccip.ui.LauncherActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushMessagingService : FirebaseMessagingService() {
    override fun onRegistered(installationId: String) {
        PushTopicManager.get(this).registered(installationId)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        val route = PushContract.route(message.data) ?: return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        createChannel(this)
        val intent = Intent(this, LauncherActivity::class.java)
            .setData(Uri.parse("opass-push:${route.pushId}"))
            .putExtra("push_id", route.pushId).putExtra("event_id", route.eventId)
            .putExtra("uri", route.uri)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val displayed = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_announcement)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true).setContentIntent(pending).build()
        NotificationManagerCompat.from(this).notify(route.pushId, 0, displayed)
    }

    companion object {
        const val CHANNEL = "announcements"
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel(CHANNEL, context.getString(R.string.announcement), NotificationManager.IMPORTANCE_DEFAULT)
                channel.enableVibration(true)
                channel.setShowBadge(false)
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }
    }
}
