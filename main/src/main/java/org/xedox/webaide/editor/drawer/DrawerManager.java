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
import org.xedox.filetree.utils.Node;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.FileX;
import org.xedox.utils.OverflowMenu;
import org.xedox.webaide.AppCore;
import org.xedox.webaide.EditorActivity;
import org.xedox.webaide.MainActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.dialog.NewFileDialog;
import org.xedox.webaide.dialog.RenameFileDialog;
import org.xedox.webaide.editor.EditorManager;
import org.xedox.webaide.project.Project;

public class DrawerManager
        implements NavigationRailView.OnItemSelectedListener,
                NavigationRailView.OnItemReselectedListener {

    private final EditorActivity context;
    private final NavigationRailView navRail;
    private final FrameLayout navContent;
    private final FileTreeFragment fileTreeFragment;
    private final Project project;
    private final TextView title;
    private final ActionBarDrawerToggle drawerToggle;
    private final DrawerLayout drawerLayout;
    private final MaterialToolbar toolbar;
    private final EditorManager editorManager;
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
        editorManager = context.getEditorManager();
        drawerToggle = new ActionBarDrawerToggle(context, drawerLayout, toolbar, 0, 0);
        drawerToggle.syncState();

        navRail.setOnItemSelectedListener(this);
        navRail.setOnItemReselectedListener(this);
        fileTreeFragment = FileTreeFragment.newInstance(project.getAbsolutePath());
        drawerLayout.addDrawerListener(drawerToggle);
        setFragment(fileTreeFragment);
        handler.post(this::setupTree);
    }

    private void setupTree() {
        fileTreeFragment
                .getFileTree()
                .setOnFileLongClickListener(
                        (view, node) -> {
                            int menu = node.isFile() ? R.menu.file : R.menu.folder;
                            OverflowMenu.show(view, menu, (i) -> handleMenu(i, node));
                        });
    }

    private void handleMenu(MenuItem item, Node node) {
        int id = item.getItemId();
        if(id == R.id.delete) {
            if(node.isFile()) node.delete();
            else FileX.deleteDirectory(node);
            fileTreeFragment.getFileTree().removeNode(node);
        } else if(id == R.id.rename){
            RenameFileDialog.show(context, fileTreeFragment.getFileTree(), node);
        }
        
        if(node.isDirectory()) {
            if(id == R.id.new_file_folder)
            NewFileDialog.show(context, fileTreeFragment.getFileTree(), node);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_file_tree) {
            setFragment(fileTreeFragment);
            return true;
        } else if (id == R.id.action_exit) {
            Intent intent = new Intent(context, MainActivity.class);
            context.finish();
            context.startActivity(intent);
        }
        return false;
    }

    public void setFragment(BaseFragment fragment) {
        FragmentManager fm = context.getSupportFragmentManager();
        fm.beginTransaction().replace(navContent.getId(), fragment).commit();
        title.setText(fragment.getTitle());
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

    public void onDestroy() {
        drawerLayout.removeDrawerListener(drawerToggle);
    }
}
