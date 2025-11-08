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
    private final List<WebViewListener> webViewListeners = new ArrayList<>();
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
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

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
                        for (WebViewListener listener : webViewListeners) {
                            listener.onPageStarted(view, url, favicon);
                        }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (progress != null) {
                            progress.setVisibility(View.GONE);
                        }
                        for (WebViewListener listener : webViewListeners) {
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
                        for (WebViewListener listener : webViewListeners) {
                            listener.onProgressChanged(view, newProgress);
                        }
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        for (WebViewListener listener : webViewListeners) {
                            listener.onReceivedTitle(view, title);
                        }
                    }

                    @Override
                    public void onReceivedIcon(WebView view, Bitmap icon) {
                        for (WebViewListener listener : webViewListeners) {
                            listener.onReceivedIcon(view, icon);
                        }
                    }

                    @Override
                    public void onShowCustomView(View view, CustomViewCallback callback) {
                        for (WebViewListener listener : webViewListeners) {
                            listener.onShowCustomView(view, callback);
                        }
                    }

                    @Override
                    public void onHideCustomView() {
                        for (WebViewListener listener : webViewListeners) {
                            listener.onHideCustomView();
                        }
                    }

                    @Override
                    public boolean onCreateWindow(
                            WebView view,
                            boolean isDialog,
                            boolean isUserGesture,
                            android.os.Message resultMsg) {
                        boolean handled = false;
                        for (WebViewListener listener : webViewListeners) {
                            if (listener.onCreateWindow(view, isDialog, isUserGesture, resultMsg)) {
                                handled = true;
                            }
                        }
                        return handled;
                    }

                    @Override
                    public void onCloseWindow(WebView window) {
                        for (WebViewListener listener : webViewListeners) {
                            listener.onCloseWindow(window);
                        }
                    }

                    @Override
                    public boolean onJsAlert(
                            WebView view,
                            String url,
                            String message,
                            android.webkit.JsResult result) {
                        for (WebViewListener listener : webViewListeners) {
                            if (listener.onJsAlert(view, url, message, result)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onJsConfirm(
                            WebView view,
                            String url,
                            String message,
                            android.webkit.JsResult result) {
                        for (WebViewListener listener : webViewListeners) {
                            if (listener.onJsConfirm(view, url, message, result)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onJsPrompt(
                            WebView view,
                            String url,
                            String message,
                            String defaultValue,
                            android.webkit.JsPromptResult result) {
                        for (WebViewListener listener : webViewListeners) {
                            if (listener.onJsPrompt(view, url, message, defaultValue, result)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
    }

    public void addWebViewListener(WebViewListener listener) {
        webViewListeners.add(listener);
    }

    public void removeWebViewListener(WebViewListener listener) {
        webViewListeners.remove(listener);
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

    public interface WebViewListener {
        default void onPageStarted(WebView view, String url, Bitmap favicon) {}

        default void onPageFinished(WebView view, String url) {}

        default void onProgressChanged(WebView view, int progress) {}

        default void onReceivedTitle(WebView view, String title) {}

        default void onReceivedIcon(WebView view, Bitmap icon) {}

        default void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {}

        default void onHideCustomView() {}

        default boolean onCreateWindow(
                WebView view,
                boolean isDialog,
                boolean isUserGesture,
                android.os.Message resultMsg) {
            return false;
        }

        default void onCloseWindow(WebView window) {}

        default boolean onJsAlert(
                WebView view, String url, String message, android.webkit.JsResult result) {
            return false;
        }

        default boolean onJsConfirm(
                WebView view, String url, String message, android.webkit.JsResult result) {
            return false;
        }

        default boolean onJsPrompt(
                WebView view,
                String url,
                String message,
                String defaultValue,
                android.webkit.JsPromptResult result) {
            return false;
        }
    }

    public interface ShouldOverrideUrlListener {
        boolean shouldOverrideUrl(WebView view, WebResourceRequest request);
    }
}
