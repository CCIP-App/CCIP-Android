package app.opass.ccip.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.network.webclient.WebChromeViewClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_web.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PuzzleFragment : Fragment() {
    companion object {
        private const val URL_NO_NETWORK = "file:///android_asset/no_network.html"
        private lateinit var mActivity: MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                view.loadUrl(URL_NO_NETWORK)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
        }
        webView.webChromeClient = WebChromeViewClient(progressBar)

        if (PreferenceUtil.getToken(activity!!) != null) {
            webView.loadUrl(
                PreferenceUtil.getCurrentEvent(mActivity).features.puzzle + toPublicToken(
                    PreferenceUtil.getToken(
                        activity!!
                    )
                )!!
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

    private fun toPublicToken(privateToken: String?): String? {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-1")
            val data = messageDigest.digest(privateToken!!.toByteArray())
            val buffer = StringBuilder()

            for (b in data) {
                buffer.append("%02x".format(b))
            }

            return buffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return null
    }
}
