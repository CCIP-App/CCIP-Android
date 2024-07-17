package app.opass.ccip.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebSettings
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentWebBinding
import app.opass.ccip.network.webclient.OfficialWebViewClient
import app.opass.ccip.network.webclient.WebChromeViewClient
import app.opass.ccip.util.CryptoUtil
import app.opass.ccip.util.PreferenceUtil

class PuzzleFragment : Fragment() {
    companion object {
        private const val URL_NO_NETWORK = "file:///android_asset/no_network.html"
        private const val EXTRA_URL = "EXTRA_URL"
        fun newInstance(url: String): PuzzleFragment = PuzzleFragment().apply {
            arguments = Bundle().apply { putString(EXTRA_URL, url) }
        }
    }

    private lateinit var mActivity: MainActivity
    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity() as MainActivity

        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webviewWrapper.setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

        val webView = binding.webView
        webView.webViewClient = OfficialWebViewClient()
        webView.webChromeClient = WebChromeViewClient(binding.progressBar, fun (request) {
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
                        CryptoUtil.toPublicToken(PreferenceUtil.getToken(mActivity)) ?: ""
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.CAMERA) && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            binding.webView.reload()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mActivity.menuInflater.inflate(R.menu.puzzle, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.puzzle_share_subject))
                intent.putExtra(Intent.EXTRA_TEXT, binding.webView.url)

                mActivity.startActivity(Intent.createChooser(intent, resources.getText(R.string.share)))
            }
        }

        return true
    }

}
