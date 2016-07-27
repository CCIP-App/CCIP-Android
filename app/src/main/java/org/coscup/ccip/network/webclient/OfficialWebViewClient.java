package org.coscup.ccip.network.webclient;

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OfficialWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
        view.loadUrl("javascript: document.querySelectorAll('[role=\"page-links\"]')[0].remove()");
        view.loadUrl("javascript: document.getElementsByTagName('footer')[0].remove()");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        view.loadUrl("file:///android_asset/no_network.html");
    }
}
