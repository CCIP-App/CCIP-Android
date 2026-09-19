package app.opass.ccip

import android.app.Application
import android.content.res.Configuration
import app.opass.ccip.push.PushMessagingService
import app.opass.ccip.push.PushTopicManager

class CCIPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PushMessagingService.createChannel(this)
        PushTopicManager.get(this).refreshAll()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        PushTopicManager.get(this).configurationChanged()
    }
}
