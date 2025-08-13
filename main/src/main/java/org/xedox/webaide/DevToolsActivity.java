package org.xedox.webaide;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import org.xedox.utils.BaseActivity;
import org.xedox.webaide.devtools.PreviewFragment;
import org.xedox.webaide.devtools.SourceFragment;

public class DevToolsActivity extends BaseActivity
        implements SearchView.OnQueryTextListener, TabLayout.OnTabSelectedListener {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ProgressBar progress;

    private PreviewFragment previewFragment;
    private SourceFragment sourceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_tools);

        toolbar = findViewById(R.id.toolbar);
        progress = findViewById(R.id.progress);
        tabLayout = findViewById(R.id.tab_layout);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        previewFragment = PreviewFragment.newInstance(progress);
        sourceFragment = SourceFragment.newInstance();
        tabLayout.addOnTabSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, previewFragment)
                .commit();
        new Handler(Looper.getMainLooper())
                .post(
                        () -> {
                            performSearch("https://www.example.com");
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

    private void performSearch(String query) {
        previewFragment.webView.loadUrl(query);
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
            performSearch(query);
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
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}
}
