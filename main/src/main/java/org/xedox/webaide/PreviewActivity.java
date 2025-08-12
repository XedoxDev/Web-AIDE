package org.xedox.webaide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.view.WebViewX;

public class PreviewActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private WebViewX webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView.setProgressBar(progress);
        webView.addOnPageStartedListener((v, url, ic)-> {
            getSupportActionBar().setSubtitle(url);
        });
        
        Intent intent = getIntent();
        String indexHtml = intent.getStringExtra("index.html");
    
        if (indexHtml != null && !indexHtml.isBlank()) {
            webView.loadUrl("file://"+indexHtml);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                loadHtmlFromUri(data);
            } else {
                webView.loadData("File found'n", "text/html", "UTF-8");
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    @Override
    public void handleBackPressed() {
    	finish();
    }

    private void loadHtmlFromUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            webView.loadData(buffer.toString(), "text/html", "UTF-8");
        } catch (Exception e) {
            webView.loadData("Ошибка загрузки: " + e.getMessage(), "text/html", "UTF-8");
        }
    }
}
