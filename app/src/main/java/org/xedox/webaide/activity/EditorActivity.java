package org.xedox.webaide.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTree;
import org.xedox.webaide.HighlightText;
import org.xedox.webaide.IDE;
import org.xedox.webaide.OverflowMenu;
import org.xedox.webaide.Project;
import org.xedox.webaide.R;
import org.xedox.webaide.adapters.EditorAdapter;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.dialogs.FileNotSavedDialog;
import org.xedox.webaide.dialogs.GitAddDialog;
import org.xedox.webaide.dialogs.GitAuthDialog;
import org.xedox.webaide.dialogs.GitStatusDialog;
import org.xedox.webaide.dialogs.NewFileDialog;
import org.xedox.webaide.dialogs.NewFolderDialog;
import org.xedox.webaide.dialogs.RenameFileDialog;
import org.xedox.webaide.editor.EditorFragment;
import org.xedox.webaide.git.GitManager;
import org.xedox.webaide.io.FileX;
import org.xedox.webaide.io.IFile;

public class EditorActivity extends BaseActivity {
    private static final String TAG = "EditorActivity";
    private static final String KEY_OPEN_FILES = "open_files";
    private static final String KEY_FILE_TREE = "file_tree_nodes";

    // Views
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 editorPager;
    private FileTree fileTree;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle drawerToggle;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private View emptyEditor;
    private TextView emptyEditorHint;
    private ConsoleLayout console;
    private View tabs;

    private EditorAdapter editorAdapter;
    private Project project;
    private GitManager git;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initializeProject();
        initializeViews();
        setupToolbar();
        setupFileTree();
        setupEditorPager();
        setupDrawer();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } 

        git = new GitManager(this, project);
    }

    @Override
    protected void onDestroy() {
        isDestroyed.set(true);
        cleanupResources();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void initializeProject() {
        String projectName = getIntent().getStringExtra("project_name");
        if (projectName == null || projectName.isEmpty()) {
            showErrorAndFinish("Project name is missing!");
            return;
        }

        try {
            project = new Project(projectName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize project", e);
            showErrorAndFinish("Failed to initialize project: " + e.getMessage());
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        editorPager = findViewById(R.id.editor_pager);
        fileTree = findViewById(R.id.file_tree);
        undoButton = findViewById(R.id.undo);
        redoButton = findViewById(R.id.redo);
        drawerLayout = findViewById(R.id.drawer_layout);
        console = findViewById(R.id.console_layout);
        tabs = findViewById(R.id.tabs);
        navView = findViewById(R.id.nav_view);
        emptyEditor = findViewById(R.id.empty_editor);
        emptyEditorHint = findViewById(R.id.empty_editor_hint);

        editorAdapter = new EditorAdapter(this);
        editorPager.setUserInputEnabled(false);
        setupClickListeners();
        setupEditorAdapterObserver();
    }

    private void setupClickListeners() {
        undoButton.setOnClickListener(v -> undo());
        redoButton.setOnClickListener(v -> redo());

        HighlightText.clickable(
                emptyEditorHint,
                getString(R.string.editor_empty_hint_see_files),
                R.color.link,
                () -> drawerLayout.open());

        HighlightText.clickable(
                emptyEditorHint,
                getString(R.string.editor_empty_hint_see_logs),
                R.color.link,
                () -> console.moveTo(getResources().getDisplayMetrics().heightPixels / 2, true));
    }

    private void setupEditorAdapterObserver() {
        editorAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        updateEditorVisibility();
                    }
                });
    }

    private void updateEditorVisibility() {
        boolean hasItems = editorAdapter.getItemCount() > 0;
        tabs.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        emptyEditor.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        tabLayout.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        undoButton.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        redoButton.setVisibility(hasItems ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setSubtitle(project != null ? project.name : getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFileTree() {
        fileTree.adapter.setOnFileClickListener((node, file, view) -> openFile(new FileX(file)));
        fileTree.adapter.setOnFileLongClickListener(this::onFileLongClick);
        fileTree.loadPath(new FileX(IDE.PROJECTS_PATH, project.name).getFullPath());
    }

    private void setupEditorPager() {
        editorPager.setAdapter(editorAdapter);
        setupTabs();
    }

    private void setupTabs() {
        new TabLayoutMediator(
                        tabLayout,
                        editorPager,
                        (tab, position) -> {
                            Fragment fragment = editorAdapter.createFragment(position);
                            if (fragment instanceof EditorFragment) {
                                EditorFragment editorFragment = (EditorFragment) fragment;
                                editorFragment.setTab(tab);
                                editorFragment.updateTabState();
                            }
                            tab.view.setOnClickListener(
                                    v -> {
                                        if (tabLayout.getSelectedTabPosition() == position) {
                                            showTabMenu(tab, position);
                                        }
                                    });
                        })
                .attach();
    }

    private void setupDrawer() {
        drawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, R.drawable.file_tree);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        drawerLayout.addDrawerListener(
                new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        setToolbarTitle(getString(R.string.file_tree));
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        setToolbarTitle(getString(R.string.app_name));
                    }
                });
    }

    private void setToolbarTitle(String newTitle) {
        if (toolbar.getTitle() != null && toolbar.getTitle().equals(newTitle)) {
            return;
        }

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(toolbar.getChildAt(0), "alpha", 1f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(toolbar.getChildAt(0), "alpha", 0f, 1f);

        fadeOut.setDuration(100);
        fadeIn.setDuration(100);

        fadeOut.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolbar.setTitle(newTitle);
                    }
                });

        animatorSet.play(fadeIn).after(fadeOut);
        animatorSet.start();
    }

    private void showTabMenu(TabLayout.Tab tab, int position) {
        OverflowMenu.show(
                this,
                tab.view,
                R.menu.tab,
                item -> {
                    if (item.getItemId() == R.id.remove) {
                        EditorFragment fragment = editorAdapter.getFragments().get(position);
                        if (fragment != null) {
                            fragment.save();
                        }
                        editorAdapter.removeFile(position);
                    }
                });
    }

    private boolean onFileLongClick(Node node, File file, View v) {
        int menuRes = node.isFile ? R.menu.file : R.menu.folder;
        OverflowMenu.show(this, v, menuRes, item -> handleFileOperation(node, item.getItemId()));
        return true;
    }

    private void handleFileOperation(Node node, int menuItemId) {
        if (menuItemId == R.id.remove) {
            deleteFile(node);
        } else if (menuItemId == R.id.rename) {
            RenameFileDialog.show(this, fileTree.adapter, node);
        } else {
            if (!node.isFile) {
                handleFolderOperation(node, menuItemId);
            }
        }
    }

    private void handleFolderOperation(Node node, int menuItemId) {
        if (menuItemId == R.id.new_file) {
            NewFileDialog.show(this, fileTree.adapter, node);
        } else if (menuItemId == R.id.new_folder) {
            NewFolderDialog.show(this, fileTree.adapter, node);
        }
    }

    private void deleteFile(Node node) {
        try {
            fileTree.adapter.deleteNode(node);
            boolean deleted = new File(node.fullPath).delete();
            if (!deleted) {
                console.printError(R.string.file_delete_failed);
            }
        } catch (SecurityException e) {
            console.printError(R.string.file_delete_permission_denied, e);
        }
    }

    public void openFile(IFile file) {
        if (isDestroyed.get()) return;

        executor.submit(
                () -> {
                    if (isDestroyed.get()) return;

                    int existingPosition = editorAdapter.findFilePosition(file);
                    handler.post(
                            () -> {
                                if (isDestroyed.get()) return;

                                if (existingPosition >= 0) {
                                    editorPager.setCurrentItem(existingPosition, true);
                                } else {
                                    editorAdapter.addFile(file);
                                    editorPager.setCurrentItem(
                                            editorAdapter.getItemCount() - 1, true);
                                }
                            });
                });
    }

    private void saveState(Bundle outState) {
        List<IFile> files = editorAdapter.getFiles();
        ArrayList<String> paths = new ArrayList<>(files.size());
        for (IFile file : files) {
            paths.add(file.getFullPath());
        }
        outState.putStringArrayList(KEY_OPEN_FILES, paths);
        outState.putString(KEY_FILE_TREE, fileTree.getRoot().fullPath);
    }

    private void restoreState(Bundle savedInstanceState) {
        ArrayList<String> paths = savedInstanceState.getStringArrayList(KEY_OPEN_FILES);
        if (paths != null) {
            editorAdapter.clear();
            for (String path : paths) {
                editorAdapter.addFile(new FileX(path));
            }
        }
        fileTree.loadPath(savedInstanceState.getString(KEY_FILE_TREE));
    }

    private void cleanupResources() {
        if (drawerLayout != null && drawerToggle != null) {
            drawerLayout.removeDrawerListener(drawerToggle);
        }
        if (git != null) {
            git.close();
        }
        if (fileTree != null) {
            fileTree.shutdown();
        }
        executor.shutdownNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.run) {
            runProject();
            return true;
        } else if (id == R.id.save) {
            saveAllFiles();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.git_init) {
            handleGitInit();
            return true;
        } else if (id == R.id.git_add) {
            handleGitAdd();
            return true;
        } else if (id == R.id.git_auth) {
            handleGitAuth();
            return true;
        } else if (id == R.id.git_push) {
            handleGitPush();
            return true;
        } else if (id == R.id.git_status) {
            handleGitStatus();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void runProject() {
        if (project == null || project.indexHtml == null) {
            showSnackbar("No index.html found in project");
            return;
        }
        saveAllFiles();
        Intent intent = new Intent(this, RunActivity.class);
        intent.putExtra("index_path", project.indexHtml.getFullPath());
        startActivity(intent);
    }

    public void saveAllFiles() {
        editorAdapter.saveAll();
    }

    private void handleGitInit() {
        try {
            git.init();
            console.printText(R.string.git_init_successful);
        } catch (Exception e) {
            console.printError(R.string.git_init_failed, e);
        }
    }
    
    private void handleGitPush() {
        try {
            git.push();
            console.printText(R.string.git_push_successful);
        } catch (Exception e) {
            console.printError(R.string.git_push_failed, e);
        }
    }
    
    private void handleGitStatus() {
        GitStatusDialog.show(this, git);
    }
    
    private void handleGitPull() {
        try {
            git.pull();
            console.printText(R.string.git_pull_successful);
        } catch (Exception e) {
            console.printError(R.string.git_pull_failed, e);
        }
    }

    private void handleGitAdd() {
        GitAddDialog.show(this, git);
    }

    private void handleGitAuth() {
        GitAuthDialog.show(this, git);
    }

    public void finish(boolean checkSaved) {
        if (!checkSaved) {
            finish();
            return;
        }

        for (EditorFragment fragment : editorAdapter.getFragments()) {
            if (!fragment.isSaved()) {
                FileNotSavedDialog.show(this);
                return;
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        finish(true);
    }

    @Override
    public void finish() {
        startActivity(new Intent(this, MainActivity.class));
        super.finish();
    }

    public void undo() {
        int position = tabLayout.getSelectedTabPosition();
        if (position >= 0 && position < editorAdapter.getFragments().size()) {
            EditorFragment fragment = editorAdapter.getFragments().get(position);
            if (fragment != null) {
                fragment.editorView.undo();
            }
        }
    }

    public void redo() {
        int position = tabLayout.getSelectedTabPosition();
        if (position >= 0 && position < editorAdapter.getFragments().size()) {
            EditorFragment fragment = editorAdapter.getFragments().get(position);
            if (fragment != null) {
                fragment.editorView.redo();
            }
        }
    }

    private void showErrorAndFinish(String message) {
        showDialog(message);
        finish();
    }
}
