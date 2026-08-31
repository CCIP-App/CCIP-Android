package app.opass.ccip.network.webclient

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class OfficialWebViewClient : WebViewClient() {

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) view.loadUrl("file:///android_asset/no_network.html")
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val intent = try {
            if (url.startsWith("intent:")) Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            else Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } catch (_: java.net.URISyntaxException) {
            return true
        }
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.component = null
        intent.selector = null

        try {
            view.context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val fallbackUrl = intent.getStringExtra("browser_fallback_url") ?: return true
            when (Uri.parse(fallbackUrl).scheme) {
                "http", "https" -> view.loadUrl(fallbackUrl)
            }
        }
        return true
    }
}
