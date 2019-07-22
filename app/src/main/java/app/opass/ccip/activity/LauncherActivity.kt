package app.opass.ccip.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.opass.ccip.model.EventConfig
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class LauncherActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent old event schema from crashing the app
        try {
            PreferenceUtil.getCurrentEvent(this)
        } catch (t: Exception) {
            t.printStackTrace()
            PreferenceUtil.setCurrentEvent(this, JsonUtil.fromJson("{\"event_id\": \"\"}", EventConfig::class.java))
        }
        val currentEventId = PreferenceUtil.getCurrentEvent(this).eventId
        if (intent.action == Intent.ACTION_VIEW) {
            val eventId = intent.data!!.getQueryParameter("event_id") ?: ""
            val token = intent.data!!.getQueryParameter("token") ?: ""

            if (eventId.isNotEmpty()) {
                if (token.isNotEmpty()) {
                    startActivity(AuthActivity.createIntent(this, eventId, token))
                } else {
                    startActivity(AuthActivity.createIntent(this, eventId, null))
                }
                return finish()
            }
        }
        if (currentEventId.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, EventActivity::class.java))
        }
        finish()
    }
}
