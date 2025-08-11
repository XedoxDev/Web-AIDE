package org.xedox.webaide.editor.drawer;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigationrail.NavigationRailView;
import org.xedox.filetree.utils.Node;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.FileX;
import org.xedox.utils.OverflowMenu;
import org.xedox.webaide.EditorActivity;
import org.xedox.webaide.MainActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.dialog.NewFileDialog;
import org.xedox.webaide.dialog.RenameFileDialog;
import org.xedox.webaide.editor.EditorManager;
import org.xedox.webaide.project.Project;

public class DrawerManager implements NavigationRailView.OnItemSelectedListener, NavigationRailView.OnItemReselectedListener {

    private final EditorActivity context;
    private final NavigationRailView navigationRail;
    private final FrameLayout navigationContent;
    private final FileTreeFragment fileTreeFragment;
    private final TextView titleView;
    private final DrawerLayout drawerLayout;
    private final MaterialToolbar toolbar;
    private final EditorManager editorManager;
    private final ActionBarDrawerToggle drawerToggle;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DrawerManager(@NonNull EditorActivity context) {
        if (context == null) throw new IllegalArgumentException();
        this.context = context;
        this.navigationRail = context.getNavRail();
        this.navigationContent = context.getNavContent();
        this.titleView = context.getNavTitle();
        this.drawerLayout = context.getDrawerLayout();
        this.toolbar = context.getToolbar();
        this.editorManager = context.getEditorManager();

        Project project = context.getProject();
        this.fileTreeFragment = FileTreeFragment.newInstance(project.getAbsolutePath());
        this.drawerToggle = new ActionBarDrawerToggle(context, drawerLayout, toolbar, 0, 0);
        drawerToggle.syncState();

        navigationRail.setOnItemSelectedListener(this);
        navigationRail.setOnItemReselectedListener(this);
        drawerLayout.addDrawerListener(drawerToggle);
        showFragment(fileTreeFragment);

        mainHandler.post(() -> {
            fileTreeFragment.getFileTree().setOnFileLongClickListener(this::handleFileLongClick);
            fileTreeFragment.getFileTree().setOnFileClickListener(this::handleFileClick);
        });
    }

    private void handleFileLongClick(View view, Node node) {
        int menuRes = node.isFile() ? R.menu.file : R.menu.folder;
        OverflowMenu.show(view, menuRes, item -> {
            if (item.getItemId() == R.id.delete) {
                if (node.isFile()) node.delete();
                else FileX.deleteDirectory(node);
                fileTreeFragment.getFileTree().removeNode(node);
            } else if (item.getItemId() == R.id.rename) {
                RenameFileDialog.show(context, fileTreeFragment.getFileTree(), node);
            } else if (item.getItemId() == R.id.new_file_folder && node.isDirectory()) {
                NewFileDialog.show(context, fileTreeFragment.getFileTree(), node);
            }
        });
    }

    private void handleFileClick(View view, Node node) {
        editorManager.openFile(node);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_file_tree) {
            showFragment(fileTreeFragment);
            return true;
        } else if (item.getItemId() == R.id.action_exit) {
            Intent intent = new Intent(context, MainActivity.class);
            context.finish();
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private void showFragment(BaseFragment fragment) {
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(navigationContent.getId(), fragment).commit();
        titleView.setText(fragment.getTitle());
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

    public void onDestroy() {
        drawerLayout.removeDrawerListener(drawerToggle);
    }
}