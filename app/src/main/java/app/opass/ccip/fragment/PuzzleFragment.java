package app.opass.ccip.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import app.opass.ccip.R;
import app.opass.ccip.network.webclient.WebChromeViewClient;
import app.opass.ccip.util.PreferenceUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import app.opass.ccip.network.webclient.WebChromeViewClient;
import app.opass.ccip.util.PreferenceUtil;

public class PuzzleFragment extends Fragment {

    private static final String URL_NO_NETWORK = "file:///android_asset/no_network.html";
    private static final String URL_PUZZLE = "https://play.coscup.org/?mode=app&token=";

    private Activity mActivity;

    private static WebView webView;
    private static ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        setHasOptionsMenu(true);

        mActivity = getActivity();
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                view.loadUrl(URL_NO_NETWORK);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeViewClient(progressBar));

        if (PreferenceUtil.getToken(getActivity()) != null) {
            webView.loadUrl(URL_PUZZLE + toPublicToken(PreferenceUtil.getToken(getActivity())));
        } else {
            webView.loadUrl("data:text/html, <div>Please login</div>");
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.puzzle, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.puzzle_share_subject));
                intent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());

                mActivity.startActivity(Intent.createChooser(intent, getResources().getText(R.string.share)));
                break;
        }

        return true;
    }

    public String toPublicToken(String privateToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(privateToken.getBytes("ASCII"));
            byte[] data = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();

            for (byte b : data) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
