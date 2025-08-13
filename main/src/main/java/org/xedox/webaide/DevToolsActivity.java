package org.xedox.webaide;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.utils.dialog.LoadingDialog;
import org.xedox.webaide.devtools.PreviewFragment;
import org.xedox.webaide.devtools.SourceFragment;
import org.xedox.webaide.devtools.WebManager;
import java.io.IOException;

public class DevToolsActivity extends BaseActivity
        implements SearchView.OnQueryTextListener,
                TabLayout.OnTabSelectedListener,
                WebManager.OnDownloadListener {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private WebManager webManager;
    private LoadingDialog loadingDialog;
    private ProgressBar progress;
    private String currentUrl;

    private PreviewFragment previewFragment;
    private SourceFragment sourceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_tools);

        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        webManager = new WebManager();
        progress = findViewById(R.id.progress);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        previewFragment = PreviewFragment.newInstance(progress);
        sourceFragment = SourceFragment.newInstance();
        tabLayout.addOnTabSelectedListener(this);
        FragmentManager fm = getSupportFragmentManager();
        if (!initedFragments) {
            initFragments(fm);
        }
    }

    private boolean initedFragments = false;

    private void initFragments(FragmentManager fm) {
        previewFragment = PreviewFragment.newInstance(progress);
        sourceFragment = SourceFragment.newInstance();

        fm.beginTransaction()
                .add(R.id.content, previewFragment)
                .add(R.id.content, sourceFragment)
                .commit();
        fm.beginTransaction().hide(sourceFragment);
    }

    private void loadUrl(String url) {
        currentUrl = url;
        loadingDialog =
                LoadingDialog.create(
                        this,
                        R.string.downloading_page_content,
                        () -> {
                            try {
                                webManager.download(url, DevToolsActivity.this);
                            } catch (IOException e) {
                                runOnUiThread(() -> ErrorDialog.show(this, e));
                                if (loadingDialog != null) {
                                    loadingDialog.dismiss();
                                }
                            }
                        });
        loadingDialog.show();
    }

    @Override
    public void onDownloadStart(int pageLength) {
        runOnUiThread(
                () -> {
                    if (loadingDialog != null) {
                        loadingDialog.setMaxProgress(pageLength);
                        loadingDialog.updateProgress("Starting download...", 0);
                    }
                });
    }

    @Override
    public void onDownload(int downloaded, int pageLength) {
        runOnUiThread(
                () -> {
                    if (loadingDialog != null) {
                        String progressText =
                                pageLength > 0
                                        ? String.format("%d/%d bytes", downloaded, pageLength)
                                        : String.format("%d bytes", downloaded);
                        loadingDialog.updateProgress(progressText, downloaded);
                    }
                });
    }

    @Override
    public void onDownloadFinished(String downloaded) {
        runOnUiThread(
                () -> {
                    sourceFragment.updateHtml(downloaded);
                    previewFragment.webView.loadDataWithBaseURL(
                            currentUrl, downloaded, "text/html", "UTF-8", null);
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public void onDownloadError(IOException e) {
        runOnUiThread(
                () -> {
                    ErrorDialog.show(this, e);
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dev_tools, menu);
        MenuItem searchItem = menu.findItem(R.id.link);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query != null && !query.isEmpty()) {
            if (!query.startsWith("http://") && !query.startsWith("https://")) {
                query = "https://" + query;
            }
            loadUrl(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) {
            showFragment(previewFragment);
        } else if (tab.getPosition() == 1) {
            showFragment(sourceFragment);
        }
    }

    private void showFragment(Fragment fragment) {
        try {
            FragmentManager fm = getSupportFragmentManager();

            fm.beginTransaction()
                    .hide(fragment == previewFragment ? sourceFragment : previewFragment)
                    .show(fragment)
                    .commit();

        } catch (Exception err) {
            ErrorDialog.show(this, err);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}
}
