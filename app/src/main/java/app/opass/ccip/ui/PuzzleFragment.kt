package app.opass.ccip.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.PermissionRequest
import android.webkit.WebSettings
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.network.webclient.OfficialWebViewClient
import app.opass.ccip.network.webclient.WebChromeViewClient
import app.opass.ccip.util.CryptoUtil
import app.opass.ccip.util.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_web.*

class PuzzleFragment : Fragment() {
    companion object {
        private const val URL_NO_NETWORK = "file:///android_asset/no_network.html"
        private const val EXTRA_URL = "EXTRA_URL"
        fun newInstance(url: String): PuzzleFragment = PuzzleFragment().apply {
            arguments = Bundle().apply { putString(EXTRA_URL, url) }
        }
    }

    private lateinit var mActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FrameLayout>(R.id.webview_wrapper)
            .setOnApplyWindowInsetsListener { v, insets ->
                v.updatePadding(bottom = insets.systemWindowInsetBottom)
                insets
            }

        webView.webViewClient = OfficialWebViewClient()
        webView.webChromeClient = WebChromeViewClient(progressBar, fun (request) {
            if (!request!!.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) request.deny()
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

        if (PreferenceUtil.getToken(mActivity) != null) {
            webView.loadUrl(
                requireArguments().getString(EXTRA_URL)!!
                    .replace(
                        "{public_token}",
                        CryptoUtil.toPublicToken(PreferenceUtil.getToken(mActivity)).toString()
                    )
            )
        } else {
            webView.loadUrl("data:text/html, <div>Please login</div>")
        }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= 21) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mActivity.menuInflater.inflate(R.menu.puzzle, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.puzzle_share_subject))
                intent.putExtra(Intent.EXTRA_TEXT, webView.url)

                mActivity.startActivity(Intent.createChooser(intent, resources.getText(R.string.share)))
            }
        }

        return true
    }

}
