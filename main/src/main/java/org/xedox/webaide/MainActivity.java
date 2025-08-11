package org.xedox.webaide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.dialog.CreateProjectDialog;
import org.xedox.webaide.editor.sora.SoraEditorManager;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectsAdapter;

public class MainActivity extends BaseActivity {

    private MaterialToolbar toolbar;

    private RecyclerView projectsRecycler;
    private ProjectsAdapter projectsAdapter;
    private View emptyProjectsRecycler;
    private ImageButton newProject;

    public static boolean initialize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        projectsRecycler = findViewById(R.id.projects_recycler_view);
        emptyProjectsRecycler = findViewById(R.id.empty_projects_recycler_layout);

        newProject = findViewById(R.id.new_project);

        setSupportActionBar(toolbar);

        projectsAdapter = new ProjectsAdapter(this);
        projectsRecycler.setLayoutManager(new LinearLayoutManager(this));
        projectsRecycler.setAdapter(projectsAdapter);
        projectsAdapter.setItems(Project.getProjectsList());
        projectsAdapter.setOnProjectClickListener(
                (view, project) -> {
                    Intent intent = new Intent(this, EditorActivity.class);
                    intent.putExtra("projectName", project.getName());
                    startActivity(intent);
                    finish();
                });
        newProject.setOnClickListener((v) -> CreateProjectDialog.show(this, projectsAdapter));
        projectsAdapter.setOnChangeListener(
                hasItems ->
                        emptyProjectsRecycler.setVisibility(hasItems ? View.GONE : View.VISIBLE));
        if (!initialize) {
            SoraEditorManager.initialize(this);
            initialize = true;
        }
    }

    @Override
    public void handleBackPressed() {
        finish();
    }
}
