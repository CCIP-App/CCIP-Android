package org.sitcon.ccip.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.sitcon.ccip.R;
import org.sitcon.ccip.network.webclient.OfficialWebViewClient;
import org.sitcon.ccip.network.webclient.WebChromeViewClient;

public class SponsorFragment extends TrackFragment {

    private static final String URL_SPONSORS = "http://coscup.org/2016/sponsors.html";
    private static WebView webView;
    private static ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_irc, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeViewClient(progressBar));
        webView.setWebViewClient(new OfficialWebViewClient());
        webView.loadUrl(URL_SPONSORS);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        return view;
    }
}
