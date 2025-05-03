package org.xedox.webaide.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
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

public class RunActivity extends BaseActivity {

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        loadToolbar();
        String indexPath = getIntent().getStringExtra("index_path");
        
        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAllowFileAccess(true);

        if (indexPath != null && new File(indexPath).exists()) {
            webview.loadUrl("file://" + indexPath);
        } else {
            webview.loadData("<h1>index.html no found!</h1>", "text/html", "UTF-8");
        }

        // https://stackoverflow.com/questions/32769505/webviewclient-onreceivederror-deprecated-new-version-does-not-detect-all-errors
        webview.setWebViewClient(
                new WebViewClient() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onReceivedError(
                            WebView view, int errorCode, String description, String failingUrl) {
                        webview.loadData(
                                String.format(
                                        "<h1>ERROR</h1><br>%s<br>Error code: %d",
                                        description, errorCode),
                                "text/html",
                                "UTF-8");
                        setSubtitle(failingUrl);
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

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        setSubtitle(url);
                    }
                });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setSubtitle(indexPath);
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
