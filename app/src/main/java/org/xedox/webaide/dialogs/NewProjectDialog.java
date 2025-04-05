package org.xedox.webaide.dialogs;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.IDE;
import org.xedox.webaide.activity.MainActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.Project;
import org.xedox.webaide.ProjectManager;
import org.xedox.webaide.adapters.ProjectsAdapter;

import java.util.Arrays;
import java.util.Objects;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class NewProjectDialog {

    private static final String TAG = "NewProjectDialog";

    public static void show(BaseActivity context) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.create_new_project);
        builder.setView(R.layout.new_project_dialog);
        
        TextInputEditText projectNameEditText = builder.findViewById(R.id.name);
        TextView errorMessageTextView = builder.findViewById(R.id.error_message);

        projectNameEditText.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.create,
                (dialog, which) -> {
                    String projectName = projectNameEditText.getText().toString().trim();

                    if (projectName.isEmpty()) {
                        errorMessageTextView.setText(R.string.project_name_empty);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return RETURN;
                    }

                    if (isProjectNameExists(projectName)) {
                        errorMessageTextView.setText(R.string.project_exists);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return RETURN;
                    }

                    Project project = ProjectManager.createProject(projectName, context);

                    if (context instanceof MainActivity) {
                        MainActivity activity = (MainActivity) context;
                        activity.projectsAdapter.add(project);
                        activity.showSnackbar(R.string.project_created_successful);
                    }

                    return EXIT;
                });

        builder.show();
    }

    private static boolean isProjectNameExists(String projectName) {
        if (IDE.PROJECTS_PATH == null || IDE.PROJECTS_PATH.list() == null) {
            return false;
        }
        return Arrays.asList(Objects.requireNonNull(IDE.PROJECTS_PATH.list()))
                .contains(projectName);
    }
}