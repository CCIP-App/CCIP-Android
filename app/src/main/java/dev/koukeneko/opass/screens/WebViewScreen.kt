package dev.koukeneko.opass.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import dev.koukeneko.opass.R
import dev.koukeneko.opass.components.AppBar

@Composable
fun WebViewScreen(
    url: String,
    navController: NavHostController,
    title: String? = null,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppBar(title = title, rightIcon = {
                IconButton(onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(browserIntent)
                }) {
                    Icon(
                        painterResource(id = R.drawable.rounded_open_in_browser_24),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Open in browser"
                    )
                }
            }, leftIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Go back")
                }
            })
        },
    ) {
        AndroidView(modifier = Modifier.padding(it), factory = { context ->
            WebView(context).apply {

                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = WebViewClient()

                settings.userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15"
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.setSupportMultipleWindows(true)
                settings.setSupportZoom(true)
            }
        }, update = { webView ->
            var temp = "https://metallica-giants-gras-admission.trycloudflare.com/"
            webView.loadUrl(url)
        })
    }
}
