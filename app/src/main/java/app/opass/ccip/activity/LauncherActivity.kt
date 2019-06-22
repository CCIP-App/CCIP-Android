package app.opass.ccip.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.opass.ccip.R
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            this@LauncherActivity.run {
                if (intent.action == Intent.ACTION_VIEW) {
                    val eventId = intent.data!!.getQueryParameter("event_id") ?: ""
                    val token = intent.data!!.getQueryParameter("token") ?: ""

                    if (eventId.isNotEmpty() && PreferenceUtil.getCurrentEvent(this).eventId != eventId) {
                        try {
                            val response = PortalClient.get().getEventConfig(eventId).asyncExecute()
                            if (response.isSuccessful) {
                                val eventConfig = response.body()!!
                                PreferenceUtil.setCurrentEvent(this, eventConfig)
                                CCIPClient.setBaseUrl(eventConfig.serverBaseUrl)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            Toast.makeText(this, R.string.offline, Toast.LENGTH_LONG).show()
                        }
                    }
                    if (token.isNotEmpty()) {
                        PreferenceUtil.setIsNewToken(this, true)
                        PreferenceUtil.setToken(this, token)
                    }
                }
                if (PreferenceUtil.getCurrentEvent(this).eventId.isNotEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, EventActivity::class.java))
                }
                finish()
            }
        }
    }
}
