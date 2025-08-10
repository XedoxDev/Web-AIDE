package org.xedox.webaide.editor.drawer;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigationrail.NavigationRailView;
import org.xedox.utils.BaseFragment;
import org.xedox.webaide.AppCore;
import org.xedox.webaide.EditorActivity;
import org.xedox.webaide.MainActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.project.Project;

public class DrawerManager
        implements NavigationRailView.OnItemSelectedListener,
                NavigationRailView.OnItemReselectedListener {

    private final EditorActivity context;
    private final NavigationRailView navRail;
    private final ViewPager2 navContent;
    private final FileTreeFragment fileTreeFragment;
    private final Project project;
    private final TextView title;
    private final ActionBarDrawerToggle drawerToggle;
    private final DrawerLayout drawerLayout;
    private final MaterialToolbar toolbar;
    private final DrawerStateAdapter drawerAdapter;
    private BaseFragment currentFragment;
    private Handler handler = new Handler(Looper.getMainLooper());

    public DrawerManager(@NonNull EditorActivity context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;

        navRail = context.getNavRail();
        navContent = context.getNavContent();
        project = context.getProject();
        title = context.getNavTitle();
        drawerLayout = context.getDrawerLayout();
        toolbar = context.getToolbar();
        drawerAdapter = new DrawerStateAdapter(context);

        if (navRail == null
                || navContent == null
                || project == null
                || title == null
                || drawerLayout == null
                || toolbar == null) {
            throw new IllegalStateException("Required views not found in activity");
        }

        drawerToggle = new ActionBarDrawerToggle(context, drawerLayout, toolbar, 0, 0);
        drawerToggle.syncState();

        navRail.setOnItemSelectedListener(this);
        navRail.setOnItemReselectedListener(this);
        navContent.setAdapter(drawerAdapter);
        // fileTreeFragment = FileTreeFragment.newInstance(project.getAbsolutePath());
        navContent.setUserInputEnabled(false);
        fileTreeFragment = FileTreeFragment.newInstance(AppCore.dir("files"));
        drawerAdapter.addFragment(fileTreeFragment);
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_file_tree) {
            // setFragment(fileTreeFragment);
            return true;
        } else if (id == R.id.action_exit) {
            Intent intent = new Intent(context, MainActivity.class);
            context.finish();
            context.startActivity(intent);
        }
        return false;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

    public void onDestroy() {
        drawerLayout.removeDrawerListener(drawerToggle);
    }
}
