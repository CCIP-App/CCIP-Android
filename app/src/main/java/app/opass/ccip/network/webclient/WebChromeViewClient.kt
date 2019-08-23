package app.opass.ccip.network.webclient

import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar

class WebChromeViewClient(private var progressBar: ProgressBar, private var permissionRequestCallback: (request: PermissionRequest?)->Unit) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        setWebProgress(newProgress)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        permissionRequestCallback(request)
    }

    private fun setWebProgress(progress: Int) {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = progress
        if (progress == 100) {
            progressBar.visibility = View.GONE
        }
    }
}
