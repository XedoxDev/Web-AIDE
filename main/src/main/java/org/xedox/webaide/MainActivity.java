package org.xedox.webaide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.dialog.WhatsNewDialog;
import org.xedox.webaide.sora.SoraEditorManager;
import org.xedox.webaide.dialog.CopyAssetsDialog;
import org.xedox.webaide.dialog.CreateProjectDialog;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectsAdapter;

public class MainActivity extends BaseActivity {

    private MaterialToolbar toolbar;

    private RecyclerView projectsRecycler;
    private ProjectsAdapter projectsAdapter;
    private View emptyProjectsRecycler;
    private ImageButton newProject, settings, devTools, whatsNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        projectsRecycler = findViewById(R.id.projects_recycler_view);
        emptyProjectsRecycler = findViewById(R.id.empty_projects_recycler_layout);

        newProject = findViewById(R.id.new_project);
        settings = findViewById(R.id.settings);
        devTools = findViewById(R.id.dev_tools);
        whatsNew = findViewById(R.id.whats_new);
        
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
        settings.setOnClickListener(
                (v) -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                });
        devTools.setOnClickListener(
                (v) -> {
                    Intent intent = new Intent(this, DevToolsActivity.class);
                    startActivity(intent);
                });
        whatsNew.setOnClickListener(
                (v) -> {
                    WhatsNewDialog.show(this);
                });
        projectsAdapter.setOnChangeListener(
                hasItems ->
                        emptyProjectsRecycler.setVisibility(hasItems ? View.GONE : View.VISIBLE));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getBoolean("isCopyedAssets", false)
                || !new File(AppCore.dir("files"), "textmate").exists()) {
            CopyAssetsDialog.show(this);
        } else if(!SoraEditorManager.initialized){
            SoraEditorManager.initialize(this);
        }
    }

    @Override
    public void handleBackPressed() {
        finish();
    }
}
