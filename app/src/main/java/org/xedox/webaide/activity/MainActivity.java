package org.xedox.webaide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.xedox.webaide.CloneRepositoryDialog;
import org.xedox.webaide.HighlightText;
import org.xedox.webaide.IDE;
import org.xedox.webaide.ProjectManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.xedox.webaide.dialogs.NewProjectDialog;
import org.xedox.webaide.adapters.ProjectsAdapter;
import static org.xedox.webaide.IDE.*;
import org.xedox.webaide.R;
import org.xedox.webaide.dialogs.WhatsNewDialog;

public class MainActivity extends BaseActivity {

    private RecyclerView projects;
    private FloatingActionButton newProject;
    private FloatingActionButton cloneRepo;
    private FloatingActionButton settings;
    private FloatingActionButton news;
    private TextView aboutApp;

    public ProjectsAdapter projectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        projects = findViewById(R.id.last_folders);
        newProject = findViewById(R.id.new_project);
        aboutApp = findViewById(R.id.about_app);
        cloneRepo = findViewById(R.id.clone_repo);
        settings = findViewById(R.id.settings);
        news = findViewById(R.id.news);
        loadToolbar();

        projectsAdapter = new ProjectsAdapter(this);
        projectsAdapter.updateProjects(ProjectManager.getProjects());

        projects.setLayoutManager(new LinearLayoutManager(this));
        projects.setAdapter(projectsAdapter);

        newProject.setOnClickListener(v -> NewProjectDialog.show(this));
        cloneRepo.setOnClickListener(v -> CloneRepositoryDialog.showStatic(this, projectsAdapter));
        news.setOnClickListener(v -> WhatsNewDialog.show(this));
        settings.setOnClickListener(
                (v) -> {
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                });
        projectsAdapter.setOnProjectClickListener(
                (project) -> {
                    Intent i = new Intent(this, EditorActivity.class);
                    i.putExtra("project_name", project.name);
                    startActivity(i);
                    finish();
                });
        HighlightText.clickable(
                aboutApp,
                "Telegram",
                R.color.link,
                () -> {
                    IDE.openLinkInBrowser(this, "https://t.me/xedox_studio");
                });
        HighlightText.clickable(
                aboutApp,
                "GitHub",
                R.color.link,
                () -> {
                    IDE.openLinkInBrowser(this, "https://github.com/XedoxDev/Web-AIDE.git");
                });
    }
}
