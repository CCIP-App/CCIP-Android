package app.opass.ccip.network.webclient;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OfficialWebViewClient extends WebViewClient {
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
