package org.xedox.webaide;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import org.xedox.utils.LocalWebServer;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.utils.view.WebViewX;

public class PreviewActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private WebViewX webView;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;

    private boolean isDesktopMode = false;
    private String currentUrl;
    private LocalWebServer localServer;
    private String localServerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        swipeRefresh = findViewById(R.id.refresh_layout);

        webView.setProgressBar(progress);
        webView.addWebViewListener(
                new WebViewX.WebViewListener() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        currentUrl = url;
                        swipeRefresh.setRefreshing(true);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        currentUrl = url;
                        swipeRefresh.setRefreshing(false);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setSubtitle(currentUrl);
                        }
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        if (getSupportActionBar() != null && title != null) {
                            getSupportActionBar().setTitle(title);
                        }
                    }
                });
        Intent intent = getIntent();
        String indexHtml = intent.getStringExtra("index.html");

        if (indexHtml != null && !indexHtml.isBlank()) {
            File htmlFile = new File(indexHtml);
            if (htmlFile.exists()) {
                startLocalServer(htmlFile.getParentFile());
                localServerUrl = "http://localhost:8080/" + htmlFile.getName();
                webView.loadUrl(localServerUrl);
                currentUrl = localServerUrl;
            } else {
                webView.loadData("File not found", "text/html", "UTF-8");
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                loadHtmlFromUri(data);
                currentUrl = data.toString();
            } else {
                webView.loadData("File not found", "text/html", "UTF-8");
            }
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(currentUrl);
        }

        swipeRefresh.setOnRefreshListener(webView::reload);

        setWebViewMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_open_browser) {
            if (currentUrl != null) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
                    browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e) {
                    ErrorDialog.show(this, new Exception("Tidak dapat membuka browser."));
                }
            } else {
                ErrorDialog.show(this, new Exception("Tidak ada URL yang bisa dibuka di browser."));
            }
            return true;

        } else if (id == R.id.action_toggle_mode) {
            isDesktopMode = !isDesktopMode;
            setWebViewMode(isDesktopMode);
            item.setTitle(isDesktopMode ? "Switch to Phone" : "Switch to Desktop");
            webView.reload();
            return true;
        } else if (id == R.id.refresh) {
            swipeRefresh.setRefreshing(false);
            webView.reload();
            swipeRefresh.setRefreshing(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setWebViewMode(boolean desktopMode) {
        WebSettings settings = webView.getSettings();
        if (desktopMode) {
            settings.setUserAgentString(
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            webView.setInitialScale(100);
        } else {
            settings.setUserAgentString(
                    "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
            settings.setUseWideViewPort(false);
            settings.setLoadWithOverviewMode(false);
            webView.setInitialScale(0);
        }
    }

    private void loadHtmlFromUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            br.close();
            is.close();
            webView.loadData(buffer.toString(), "text/html", "UTF-8");
        } catch (Exception e) {
            webView.loadData("Error loading file: " + e.getMessage(), "text/html", "UTF-8");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocalServer();
    }

    private void startLocalServer(File rootDir) {
        try {
            stopLocalServer();
            localServer = new LocalWebServer(8080, rootDir);
            localServer.start();
        } catch (IOException e) {
            ErrorDialog.show(this, new Exception("Gagal memulai server lokal: " + e.getMessage()));
        }
    }

    private void stopLocalServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }
}
