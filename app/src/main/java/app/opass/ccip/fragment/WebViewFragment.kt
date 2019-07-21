package app.opass.ccip.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.network.webclient.OfficialWebViewClient
import app.opass.ccip.network.webclient.WebChromeViewClient
import kotlinx.android.synthetic.main.fragment_web.*

class WebViewFragment : Fragment() {
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
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            if (arguments!!.getBoolean(EXTRA_SHOULD_USE_BUILTIN_ZOOM_CONTROLS)) {
                builtInZoomControls = true
                displayZoomControls = false
            }
            if (Build.VERSION.SDK_INT >= 21) mixedContentMode = MIXED_CONTENT_COMPATIBILITY_MODE
        }
        webView.loadUrl(arguments!!.getString(EXTRA_URL))
    }

    companion object {
        private const val EXTRA_URL = "EXTRA_URL"
        private const val EXTRA_SHOULD_USE_BUILTIN_ZOOM_CONTROLS = "EXTRA_SHOULD_USE_BUILTIN_ZOOM_CONTROLS"

        fun newInstance(url: String, shouldUseBuiltinZoomControls: Boolean = false): WebViewFragment {
            val bundle = Bundle().apply {
                putString(EXTRA_URL, url)
                putBoolean(EXTRA_SHOULD_USE_BUILTIN_ZOOM_CONTROLS, shouldUseBuiltinZoomControls)
            }
            return WebViewFragment().apply { arguments = bundle }
        }
    }
}
