package org.sitcon.ccip.network.webclient;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OfficialWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
        view.loadUrl("javascript: document.querySelectorAll('[role=\"banner\"]')[0].remove();" +
                "document.querySelectorAll('[role=\"cover-background-image\"]')[0].remove();" +
                "document.getElementsByTagName('footer')[0].remove()");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        view.loadUrl("file:///android_asset/no_network.html");
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        return true;
    }
}
