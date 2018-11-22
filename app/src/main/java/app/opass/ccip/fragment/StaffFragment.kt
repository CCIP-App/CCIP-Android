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

class StaffFragment : Fragment() {
    companion object {
        private const val URL_STAFFS = "https://summit.g0v.tw/2018/staff/?mode=app"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_web, container, false)

        webView.webChromeClient = WebChromeViewClient(progressBar)
        webView.webViewClient = OfficialWebViewClient()
        webView.loadUrl(URL_STAFFS)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= 21) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        return view
    }
}
