package app.opass.ccip.util

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE
import android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.core.content.getSystemService
import app.opass.ccip.model.WifiNetworkInfo

fun String.wrapInQuotes(): String = "\"$this\""

object WifiUtil {
    fun installNetwork(context: Context, info: WifiNetworkInfo): Boolean {
        val hasPassword = !info.password.isNullOrEmpty()
        val manager = context.getSystemService<WifiManager>() ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestion = WifiNetworkSuggestion.Builder().apply {
                setSsid(info.ssid)
                if (hasPassword) setWpa2Passphrase(info.password!!)
            }.build()
            val status = manager.addNetworkSuggestions(listOf(suggestion))
            return status == STATUS_NETWORK_SUGGESTIONS_SUCCESS || status == STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE
        } else {
            val config = WifiConfiguration().apply {
                SSID = info.ssid.wrapInQuotes()
                if (hasPassword) preSharedKey = info.password!!.wrapInQuotes()
            }

            val networkId = manager.addNetwork(config)
            if (networkId != -1) {
                manager.enableNetwork(networkId, false)
                return true
            }
            return false
        }
    }
}
