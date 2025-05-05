package org.xedox.webaide.activity;

import android.animation.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.*;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.*;
import java.lang.reflect.Method;
import org.xedox.webaide.*;
import org.xedox.webaide.activity.editor.*;
import org.xedox.webaide.editor.*;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.dialogs.FileNotSavedDialog;
import org.xedox.webaide.editor.EditorFragment;
import org.xedox.webaide.git.GitManager;
import org.xedox.webaide.io.*;
import java.util.*;

public class EditorActivity extends BaseActivity {
    private static final String TAG = "EditorActivity", KEY_OPEN_FILES = "open_files";
    private TabLayout tabLayout;
    private ViewPager2 editorPager;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle drawerToggle;
    private ImageButton undoButton, redoButton;
    private View emptyEditor, tabs;
    private TextView emptyEditorHint;
    private ConsoleLayout console;

    private EditorAdapter editorAdapter;
    private Project project;
    private GitManager git;
    private EditorGitManager editorGitManager;
    private EditorFileTreeManager fileTreeManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        String projectName = getIntent().getStringExtra("project_name");
        project = new Project(projectName);

        tabLayout = findViewById(R.id.tab_layout);
        editorPager = findViewById(R.id.editor_pager);
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
        fileTreeManager =
                new EditorFileTreeManager(
                        this, findViewById(R.id.file_tree), project, editorAdapter, console, git);
        loadToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setSubtitle(project != null ? project.name : getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        editorPager.setAdapter(editorAdapter);
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
                                        if (tabLayout.getSelectedTabPosition() == position)
                                            showTabMenu(tab, position);
                                    });
                        })
                .attach();

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

        undoButton.setOnClickListener(v -> getCurrentFragment().editorView.undo());
        redoButton.setOnClickListener(v -> getCurrentFragment().editorView.redo());
        HighlightText.clickable(
                emptyEditorHint,
                getString(R.string.editor_empty_hint_see_files),
                R.color.link,
                () -> drawerLayout.open());
        HighlightText.clickable(
                emptyEditorHint,
                getString(R.string.editor_empty_hint_see_logs),
                R.color.link,
                () -> console.moveTo(getResources().getDisplayMetrics().heightPixels / 2));

        editorAdapter.setOnChangeListener(
                () -> {
                    boolean hasItems = editorAdapter.getItemCount() > 0;
                    tabs.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    emptyEditor.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                    tabLayout.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    undoButton.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    redoButton.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                });
        editorAdapter.change();

        if (savedInstanceState != null) {
            ArrayList<String> paths = savedInstanceState.getStringArrayList(KEY_OPEN_FILES);
            if (paths != null) {
                editorAdapter.clear();
                for (String path : paths) editorAdapter.addFile(new FileX(path));
            }
        }

        git = new GitManager(this, project);
        editorGitManager = new EditorGitManager(this, git, console);

        getOnBackPressedDispatcher()
                .addCallback(
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {
                                finish(true);
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        if (drawerLayout != null) drawerLayout.removeDrawerListener(drawerToggle);
        if (git != null) git.close();
        if (fileTreeManager != null) fileTreeManager.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        ArrayList<String> paths = new ArrayList<>();
        for (IFile file : editorAdapter.getFiles()) paths.add(file.getFullPath());
        outState.putStringArrayList(KEY_OPEN_FILES, paths);
        super.onSaveInstanceState(outState);
    }

    private void showTabMenu(TabLayout.Tab tab, int position) {
        OverflowMenu.show(
                this,
                tab.view,
                R.menu.tab,
                item -> {
                    if (item.getItemId() == R.id.remove) {
                        EditorFragment fragment = editorAdapter.getFragments().get(position);
                        if (fragment != null) fragment.save();
                        editorAdapter.removeFile(position);
                    }
                });
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
            if (project == null || project.indexHtml == null) {
                showSnackbar("No index.html found in project");
                return true;
            }
            editorAdapter.saveAll();
            startActivity(
                    new Intent(this, RunActivity.class)
                            .putExtra("index_path", project.indexHtml.getFullPath()));
            return true;
        } else if (id == R.id.save) {
            editorAdapter.saveAll();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.settings) {
            startActivity(
                    new Intent(this, SettingsActivity.class)
                            .putExtra("project_name", project.name));
            saveAllFiles();
            super.finish();
            return true;
        }
        return editorGitManager.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m =
                            menu.getClass()
                                    .getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e("OverflowMenu", "Error forcing menu icons to show", e);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void finish(boolean checkSaved) {
        if (checkSaved) {
            for (EditorFragment fragment : editorAdapter.getFragments()) {
                if (!fragment.isSaved()) {
                    FileNotSavedDialog.show(this);
                    return;
                }
            }
        }
        finish();
    }

    @Override
    public void finish() {
        startActivity(
                new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        super.finish();
    }

    private EditorFragment getCurrentFragment() {
        int position = tabLayout.getSelectedTabPosition();
        return (position >= 0 && position < editorAdapter.getFragments().size())
                ? editorAdapter.getFragments().get(position)
                : null;
    }

    public ViewPager2 getEditorPager() {
        return editorPager;
    }

    public void saveAllFiles() {
        editorAdapter.saveAll();
    }
}