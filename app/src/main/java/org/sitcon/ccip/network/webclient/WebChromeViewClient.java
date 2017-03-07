package org.sitcon.ccip.network.webclient;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class WebChromeViewClient extends WebChromeClient {
    ProgressBar progressBar;

    public WebChromeViewClient(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        setWebProgress(newProgress);
    }

    public void setWebProgress(int progress) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(progress);
        if (progress == 100) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
