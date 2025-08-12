package org.xedox.utils.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class WebViewX extends WebView {

    private ProgressBar progress;
    private final List<OnPageStartedListener> pageStartedListeners = new ArrayList<>();
    private final List<OnPageFinishedListener> pageFinishedListeners = new ArrayList<>();
    private final List<OnProgressChangedListener> progressChangedListeners = new ArrayList<>();
    private final List<ShouldOverrideUrlListener> shouldOverrideUrlListeners = new ArrayList<>();

    public WebViewX(Context context) {
        super(context);
        init();
    }

    public WebViewX(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewX(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WebViewX(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        WebSettings settings = getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view, WebResourceRequest request) {
                        for (ShouldOverrideUrlListener listener : shouldOverrideUrlListeners) {
                            if (listener.shouldOverrideUrl(view, request)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        if (progress != null) {
                            progress.setVisibility(View.VISIBLE);
                            progress.setProgress(0);
                        }
                        for (OnPageStartedListener listener : pageStartedListeners) {
                            listener.onPageStarted(view, url, favicon);
                        }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (progress != null) {
                            progress.setVisibility(View.GONE);
                        }
                        for (OnPageFinishedListener listener : pageFinishedListeners) {
                            listener.onPageFinished(view, url);
                        }
                    }
                });

        setWebChromeClient(
                new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        if (progress != null) {
                            progress.setProgress(newProgress);
                        }
                        for (OnProgressChangedListener listener : progressChangedListeners) {
                            listener.onProgressChanged(view, newProgress);
                        }
                    }
                });
    }

    public void addOnPageStartedListener(OnPageStartedListener listener) {
        pageStartedListeners.add(listener);
    }

    public void removeOnPageStartedListener(OnPageStartedListener listener) {
        pageStartedListeners.remove(listener);
    }

    public void addOnPageFinishedListener(OnPageFinishedListener listener) {
        pageFinishedListeners.add(listener);
    }

    public void removeOnPageFinishedListener(OnPageFinishedListener listener) {
        pageFinishedListeners.remove(listener);
    }

    public void addOnProgressChangedListener(OnProgressChangedListener listener) {
        progressChangedListeners.add(listener);
    }

    public void removeOnProgressChangedListener(OnProgressChangedListener listener) {
        progressChangedListeners.remove(listener);
    }

    public void addShouldOverrideUrlListener(ShouldOverrideUrlListener listener) {
        shouldOverrideUrlListeners.add(listener);
    }

    public void removeShouldOverrideUrlListener(ShouldOverrideUrlListener listener) {
        shouldOverrideUrlListeners.remove(listener);
    }

    public void setProgressBar(ProgressBar progress) {
        this.progress = progress;
    }

    public interface OnPageStartedListener {
        void onPageStarted(WebView view, String url, Bitmap favicon);
    }

    public interface OnPageFinishedListener {
        void onPageFinished(WebView view, String url);
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(WebView view, int progress);
    }

    public interface ShouldOverrideUrlListener {
        boolean shouldOverrideUrl(WebView view, WebResourceRequest request);
    }
}
