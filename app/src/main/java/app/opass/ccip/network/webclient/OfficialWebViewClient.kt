package app.opass.ccip.network.webclient

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
        view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return true
    }
}
