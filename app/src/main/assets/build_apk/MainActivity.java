package $package$;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import $package$.R;

public class MainActivity extends Activity {
    WebViewX webView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        webView = new WebViewX(this);
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu arg0) {
        getMenuInflater().inflate(R.menu.main, arg0);
        return super.onCreateOptionsMenu(arg0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem arg0) {
        int id = arg0.getItemId();
        if (id == R.id.back) {
            webView.goBack();
        }
        if (id == R.id.forward) {
            webView.goForward();
        }
        if (id == R.id.refresh) {
            webView.reload();
        }
        return super.onOptionsItemSelected(arg0);
    }

    public class WebViewX extends WebView {

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
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            return true;
                        }
                    });
        }
    }
}
