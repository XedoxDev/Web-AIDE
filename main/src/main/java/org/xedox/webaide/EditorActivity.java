package org.xedox.webaide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentContainerView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Map;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.dialog.ColorPickerDialog;
import org.xedox.webaide.editor.EditorManager;
import org.xedox.webaide.editor.drawer.DrawerManager;
import org.xedox.webaide.project.Project;

public class EditorActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Project project;

    private DrawerManager drawerManager;
    private EditorManager editorManager;

    private NavigationRailView navRail;
    private FrameLayout navContent;
    private View nav;
    private TextView navTitle;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_editor);
            toolbar = findViewById(R.id.toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            tabLayout = findViewById(R.id.tab_layout);
            viewPager = findViewById(R.id.view_pager);
            navRail = findViewById(R.id.nav_rail);
            navContent = findViewById(R.id.nav_content);
            navTitle = findViewById(R.id.title);
            nav = findViewById(R.id.nav);
            setSupportActionBar(toolbar);

            Intent intent = getIntent();
            project = Project.getProject(intent.getStringExtra("projectName"));
            getSupportActionBar().setSubtitle(project.getName());
            editorManager = new EditorManager(this);
            drawerManager = new DrawerManager(this);
        } catch (Exception err) {
            ErrorDialog.show(this, err);
        }
    }

    @Override
    public void handleBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (editorManager.onOptionsItemSelected(item)) return true;
        if (item.getItemId() == R.id.run) {
            try {
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putExtra("index.html", project.file("index.html"));
                startActivity(intent);
            } catch (Throwable err) {
                ErrorDialog.show(getApplicationContext(), err);
            }
        } else if (item.getItemId() == R.id.color_picker) {
            ColorPickerDialog.show(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return onCreateOptionsMenu(R.menu.editor, menu);
    }

    public MaterialToolbar getToolbar() {
        return this.toolbar;
    }

    public DrawerLayout getDrawerLayout() {
        return this.drawerLayout;
    }

    public TabLayout getTabLayout() {
        return this.tabLayout;
    }

    public ViewPager2 getViewPager() {
        return this.viewPager;
    }

    public DrawerManager getDrawerManager() {
        return this.drawerManager;
    }

    public EditorManager getEditorManager() {
        return this.editorManager;
    }

    public NavigationRailView getNavRail() {
        return this.navRail;
    }

    public FrameLayout getNavContent() {
        return this.navContent;
    }

    public Project getProject() {
        return this.project;
    }

    public TextView getNavTitle() {
        return this.navTitle;
    }

    @Override
    protected void onDestroy() {
        if (drawerManager != null) drawerManager.onDestroy();
        if (editorManager != null) editorManager.onDestroy();
        super.onDestroy();
    }
}
