package org.xedox.webaide.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import org.xedox.webaide.R;

import java.io.File;
import java.lang.reflect.Method;
import org.xedox.webaide.dialogs.DialogBuilder;

public class RunActivity extends BaseActivity {

    private static final String TAG = "RunActivity";
    private static final String DEFAULT_DEVICE = "mobile";

    private WebView webView;
    private ProgressBar progressBar;
    private String currentUrl;
    private String currentDevice = DEFAULT_DEVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        loadToolbar();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        configureWebView();
        loadInitialUrl();
    }

    private void initializeViews() {
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        //settings.setDatabaseEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(title);
                }
            }
        });
    }

    private void loadInitialUrl() {
        String indexPath = getIntent().getStringExtra("index_path");
        currentUrl = (indexPath != null && new File(indexPath).exists()) ? "file://" + indexPath : null;
        if (currentUrl != null) webView.loadUrl(currentUrl);
    }

    private void applyDeviceSettings() {
        String userAgent;
        
        if (currentDevice.equals("tablet")) {
            userAgent = "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15";
        } else if (currentDevice.equals("desktop")) {
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        } else {
            userAgent = "Mozilla/5.0 (Linux; Android 10) Mobile Safari/537.36";
        }

        webView.getSettings().setUserAgentString(userAgent);
        if (currentUrl != null) webView.reload();
    }

    private void showInDialog(String title, String content) {
        new DialogBuilder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.ok, (d, w) -> d.dismiss())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.run_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
                Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error forcing menu icons to show", e);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.back) {
            if (webView.canGoBack()) webView.goBack();
            return true;
        } else if (id == R.id.forward) {
            if (webView.canGoForward()) webView.goForward();
            return true;
        } else if (id == R.id.refresh) {
            webView.reload();
            return true;
        } else if (id == R.id.device_mobile) {
            currentDevice = "mobile";
            item.setChecked(true);
            applyDeviceSettings();
            return true;
        } else if (id == R.id.device_tablet) {
            currentDevice = "tablet";
            item.setChecked(true);
            applyDeviceSettings();
            return true;
        } else if (id == R.id.device_desktop) {
            currentDevice = "desktop";
            item.setChecked(true);
            applyDeviceSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}