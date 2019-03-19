package app.opass.ccip.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.network.webclient.OfficialWebViewClient
import app.opass.ccip.network.webclient.WebChromeViewClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_web.*

class StaffFragment : Fragment() {
    private lateinit var mActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webChromeClient = WebChromeViewClient(progressBar)
        webView.webViewClient = OfficialWebViewClient()
        webView.loadUrl(PreferenceUtil.getCurrentEvent(mActivity).features.staffs)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= 21) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }
}
