package app.opass.ccip.util

import android.app.Application
import android.content.Intent
import android.net.Uri
import app.opass.ccip.activity.MainActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal

class NotificationOpenedHandler(private val context: Application) : OneSignal.NotificationOpenedHandler {
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val launchUrl = result.notification.payload.launchURL
        val intent = if (launchUrl != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(launchUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.ARG_IS_FROM_NOTIFICATION, true)
        }
        context.startActivity(intent)
    }
}
