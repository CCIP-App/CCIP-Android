package app.opass.ccip.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.opass.ccip.R
import app.opass.ccip.R.string.*
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.CCIPClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region

class BeaconReceiver(private val context: Context) : MonitorNotifier, CoroutineScope by CoroutineScope(Main) {
    companion object {
        private const val CHANNEL_ID = "beacon_notify"
    }

    override fun didDetermineStateForRegion(stats: Int, region: Region) {
        Log.d("BeaconNotify", "Init stats: $stats, $region")
    }

    override fun didEnterRegion(region: Region) {
        Log.d("BeaconNotify", "enter: $region")

        if (PreferenceUtil.isBeaconNotified(context)) return

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Register notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                context.getString(beacon_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).let(manager::createNotificationChannel)
        }
        // Check login and check in status
        val token = PreferenceUtil.getToken(context)

        if (token == null) {
            val content = context.getString(beacon_notify_need_login)

            // Build and send notify
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_announcement_black_48dp)
                .setContentTitle(context.getString(app_name))
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent().setClass(context, MainActivity::class.java),
                        0
                    )
                )
                .build()
                .let { manager.notify(System.currentTimeMillis().toInt(), it) }
        } else {
            launch {
                // check has been checked in
                try {
                    if (CCIPClient.get().status(token).asyncExecute().body()?.scenarios?.any { it.used != null } != false) return@launch
                } catch (e: Exception) {
                    return@launch
                }

                // Build and send notify
                val content = context.getString(beacon_notify_need_checkin)

                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_announcement_black_48dp)
                    .setContentTitle(context.getString(app_name))
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            0,
                            Intent().setClass(context, MainActivity::class.java),
                            0
                        )
                    )
                    .build()
                    .let { manager.notify(System.currentTimeMillis().toInt(), it) }
            }
        }
        PreferenceUtil.setBeaconNotified(context)
    }

    override fun didExitRegion(region: Region) {
        Log.d("BeaconNotify", "exit: $region")
    }
}
