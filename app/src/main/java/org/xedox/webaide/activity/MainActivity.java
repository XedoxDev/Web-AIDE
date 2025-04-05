package org.xedox.webaide.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xedox.webaide.CloneRepositoryDialog;
import org.xedox.webaide.HighlightText;
import org.xedox.webaide.ProjectManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.xedox.webaide.dialogs.NewProjectDialog;
import org.xedox.webaide.adapters.ProjectsAdapter;
import static org.xedox.webaide.IDE.*;
import org.xedox.webaide.R;

public class MainActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private RecyclerView projects;
    public ProjectsAdapter projectsAdapter;
    private FloatingActionButton newProject;
    private FloatingActionButton cloneRepo;
    private TextView aboutApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        projects = findViewById(R.id.last_folders);
        newProject = findViewById(R.id.new_project);
        aboutApp = findViewById(R.id.about_app);
        cloneRepo = findViewById(R.id.clone_repo);
        setSupportActionBar(toolbar);

        projectsAdapter = new ProjectsAdapter(this);
        projectsAdapter.updateProjects(ProjectManager.getProjects());

        projects.setLayoutManager(new LinearLayoutManager(this));
        projects.setAdapter(projectsAdapter);

        newProject.setOnClickListener(v -> NewProjectDialog.show(this));
        cloneRepo.setOnClickListener(v -> CloneRepositoryDialog.showStatic(this, projectsAdapter));
        
        projectsAdapter.setOnProjectClickListener(
                (project) -> {
                    Intent i = new Intent(this, EditorActivity.class);
                    i.putExtra("project_name", project.name);
                    startActivity(i);
                    finish();
                });
        HighlightText.clickable(
                aboutApp,
                "Xedox",
                R.color.link,
                () -> {
                    Intent i =
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/XedoxDev"));
                    startActivity(i);
                });
    }
}
