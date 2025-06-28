package org.xedox.webaide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xedox.webaide.CloneRepositoryDialog;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.util.HighlightText;
import org.xedox.webaide.IDE;
import org.xedox.webaide.project.ProjectManager;
import org.xedox.webaide.dialogs.NewProjectDialog;
import org.xedox.webaide.project.ProjectsAdapter;
import org.xedox.webaide.R;
import org.xedox.webaide.dialogs.WhatsNewDialog;

import java.lang.reflect.Method;

public class MainActivity extends BaseActivity {

    private RecyclerView projects;
    private FloatingActionButton newProject;
    private TextView aboutApp;
    public ProjectsAdapter projectsAdapter;
    private View emptyProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadToolbar();
        projects = findViewById(R.id.last_folders);
        newProject = findViewById(R.id.new_project);
        aboutApp = findViewById(R.id.about_app);
        emptyProjects = findViewById(R.id.empty_projects);

        projectsAdapter = new ProjectsAdapter(this);
        projectsAdapter.updateProjects(ProjectManager.getProjects());

        projects.setLayoutManager(new LinearLayoutManager(this));
        projects.setAdapter(projectsAdapter);

        newProject.setOnClickListener(v -> NewProjectDialog.show(this));

        projectsAdapter.setOnProjectClickListener(
                project -> {
                    Intent i = new Intent(this, EditorActivity.class);
                    i.putExtra("project_name", project.name);
                    startActivity(i);
                    finish();
                });

        HighlightText.clickable(
                aboutApp,
                "Telegram",
                R.color.link,
                () -> openLinkInBrowser("https://t.me/xedox_studio"));

        HighlightText.clickable(
                aboutApp,
                "GitHub",
                R.color.link,
                () -> openLinkInBrowser("https://github.com/XedoxDev/Web-AIDE.git"));

        projectsAdapter.setOnChangeListener(
                () -> {
                    int numberOfProjects = projectsAdapter.getItemCount();
                    if (numberOfProjects <= 0) {
                        emptyProjects.setVisibility(View.VISIBLE);
                        projects.setVisibility(View.GONE);
                    } else {
                        emptyProjects.setVisibility(View.GONE);
                        projects.setVisibility(View.VISIBLE);
                    }
                });

        projectsAdapter.change();

        View toolbarTitle = toolbar.getChildAt(0);
        toolbarTitle.setOnClickListener(
                v -> {
                    new DialogBuilder(this)
                            .setTitle("You touch MY TITLE")
                            .setMessage("BRO, WHY YOU DO IT????")
                            .show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m =
                        menu.getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                Log.e("OverflowMenu", "Error forcing menu icons to show", e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return true;
        } else if (id == R.id.clone_repo) {
            CloneRepositoryDialog.show(this, projectsAdapter);
            return true;
        } else if (id == R.id.news) {
            WhatsNewDialog.show(this);
            return true;
        } else if (id == R.id.open_parser) {
            startActivity(new Intent(this, UrlSourceActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
