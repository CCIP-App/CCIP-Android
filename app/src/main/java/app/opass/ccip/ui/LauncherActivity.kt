package app.opass.ccip.ui

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.opass.ccip.R
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.model.FeatureType
import app.opass.ccip.network.PortalClient
import app.opass.ccip.push.PushContract
import app.opass.ccip.ui.auth.AuthActivity
import app.opass.ccip.ui.event.EventActivity
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentEventId = PreferenceUtil.getCurrentEvent(this).eventId
        val isLaunchedFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0

        val data = listOf("push_id", "event_id", "uri").mapNotNull { key ->
            intent.getStringExtra(key)?.let { key to it }
        }.toMap()
        val push = if (isLaunchedFromHistory) null else PushContract.route(data)
        if (push != null) {
            lifecycleScope.launch {
                try {
                    if (push.uri != null) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(push.uri)))
                    } else {
                        val event = PreferenceUtil.getEvent(this@LauncherActivity, push.eventId)
                            ?: PortalClient.get().getEventConfig(push.eventId).asyncExecute().body()
                        check(event?.eventId == push.eventId && event.features.any { it.feature == FeatureType.ANNOUNCEMENT && it.url != null })
                        PreferenceUtil.setCurrentEvent(this@LauncherActivity, event)
                        startActivity(Intent(this@LauncherActivity, MainActivity::class.java)
                            .putExtra(MainActivity.ARG_IS_FROM_NOTIFICATION, true))
                    }
                } catch (_: Exception) {
                    Toast.makeText(this@LauncherActivity, R.string.unexpected_error_try_again, Toast.LENGTH_LONG).show()
                } finally { finish() }
            }
            return
        }

        if (intent.action == Intent.ACTION_VIEW && !isLaunchedFromHistory) {
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
