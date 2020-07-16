package app.opass.ccip.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE
import android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import app.opass.ccip.R
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
        val args = requireArguments()

        view.findViewById<FrameLayout>(R.id.webview_wrapper)
            .setOnApplyWindowInsetsListener { v, insets ->
                v.updatePadding(bottom = insets.systemWindowInsetBottom)
                insets
            }

        webView.webChromeClient = WebChromeViewClient(progressBar, fun (request) {
            if (!request!!.resources.contains(RESOURCE_VIDEO_CAPTURE)) request.deny()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android M Permission check
                if (mActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 2)
                    request.deny()
                } else {
                    request.grant(request.resources)
                }
            } else {
                request.grant(request.resources)
            }
        })
        webView.webViewClient = OfficialWebViewClient()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            if (args.getBoolean(EXTRA_SHOULD_USE_BUILTIN_ZOOM_CONTROLS)) {
                builtInZoomControls = true
                displayZoomControls = false
            }
            if (Build.VERSION.SDK_INT >= 21) mixedContentMode = MIXED_CONTENT_COMPATIBILITY_MODE
        }
        webView.loadUrl(args.getString(EXTRA_URL))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.CAMERA) && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            webView.reload()
        }
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
