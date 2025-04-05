package org.xedox.webaide.activity;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.File;
import org.xedox.webaide.R;

public class RunActivity extends AppCompatActivity {

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        String indexPath = getIntent().getStringExtra("index_path");
        Log.d("RunActivity", "Загрузка: " + indexPath);

        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAllowFileAccess(true); // Важно для локальных файлов!

        if (indexPath != null && new File(indexPath).exists()) {
            webview.loadUrl("file://" + indexPath);
        } else {
            webview.loadData("<h1>index.html не найден</h1>", "text/html", "UTF-8");
        }

        // https://stackoverflow.com/questions/32769505/webviewclient-onreceivederror-deprecated-new-version-does-not-detect-all-errors
        webview.setWebViewClient(
                new WebViewClient() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onReceivedError(
                            WebView view, int errorCode, String description, String failingUrl) {
                        webview.loadData("<h1>Ошибка</h1><p>" + description + "</p>", "text/html", "UTF-8");
                    }

                    @TargetApi(android.os.Build.VERSION_CODES.M)
                    @Override
                    public void onReceivedError(
                            WebView view, WebResourceRequest req, WebResourceError rerr) {
                        onReceivedError(
                                view,
                                rerr.getErrorCode(),
                                rerr.getDescription().toString(),
                                req.getUrl().toString());
                    }
                });

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
