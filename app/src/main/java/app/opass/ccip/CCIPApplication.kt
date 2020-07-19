package app.opass.ccip

import android.app.Application
import app.opass.ccip.util.NotificationOpenedHandler
import com.onesignal.OneSignal

class CCIPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignal.startInit(this).setNotificationOpenedHandler(NotificationOpenedHandler(this)).init()
    }
}
