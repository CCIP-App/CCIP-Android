package app.opass.ccip.network.webclient

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class OfficialWebViewClient : WebViewClient() {
    @Deprecated("Deprecated in Java")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) view.loadUrl("file:///android_asset/no_network.html")
    }

    @TargetApi(Build.VERSION_CODES.M)
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
