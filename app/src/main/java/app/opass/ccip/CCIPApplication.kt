package app.opass.ccip

import android.app.Application
import app.opass.ccip.util.NotificationClickListener
import com.onesignal.OneSignal

class CCIPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignal.initWithContext(this, "b6213f49-e356-4b48-aa9d-7cf10ce1904d")
        OneSignal.Notifications.addClickListener(NotificationClickListener(this))
    }
}
