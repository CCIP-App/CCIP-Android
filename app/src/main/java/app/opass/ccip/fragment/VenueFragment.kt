package app.opass.ccip.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.network.webclient.OfficialWebViewClient
import app.opass.ccip.network.webclient.WebChromeViewClient
import kotlinx.android.synthetic.main.fragment_web.*

class VenueFragment : Fragment() {
    companion object {
        private val URL_SPONSORS = "https://summit.g0v.tw/2018/agenda/?mode=app#maps"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webChromeClient = WebChromeViewClient(progressBar)
        webView.webViewClient = OfficialWebViewClient()
        webView.loadUrl(URL_SPONSORS)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        if (Build.VERSION.SDK_INT >= 21) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }
}
