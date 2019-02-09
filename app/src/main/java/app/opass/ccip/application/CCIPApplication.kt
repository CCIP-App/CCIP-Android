package app.opass.ccip.application

import android.app.Application
import com.onesignal.OneSignal

class CCIPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignal.startInit(this).init()
    }
}
