package app.opass.ccip.util

import android.app.Application
import android.content.Intent
import android.net.Uri
import app.opass.ccip.ui.MainActivity
import com.onesignal.OSNotification
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal

class NotificationOpenedHandler(private val context: Application) : OneSignal.NotificationOpenedHandler {
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val launchUrl = result.notification.payload.launchURL
        if (launchUrl != null) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(launchUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            if (result.notification.displayType == OSNotification.DisplayType.InAppAlert) return
            if (PreferenceUtil.getCurrentEvent(context).eventId.isEmpty()) return

            val intent = Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.ARG_IS_FROM_NOTIFICATION, true)
            context.startActivity(intent)
        }
    }
}
