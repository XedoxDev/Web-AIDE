package org.xedox.webaide.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.xedox.webaide.IDE;
import org.xedox.webaide.R;
import org.xedox.webaide.ProjectManager;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class RenameProjectDialog {
    private static final String TAG = "RenameProjectDialog";

    public static void show(Context context, String oldProjectName, int projectPosition) {

        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.rename_project);
        builder.setView(R.layout.rename_project_dialog);

        TextInputEditText projectNameEditText = builder.findViewById(R.id.name);
        TextView errorMessageTextView = builder.findViewById(R.id.error_message);

        projectNameEditText.setText(oldProjectName);
        projectNameEditText.requestFocus();
        projectNameEditText.post(() -> projectNameEditText.selectAll());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.rename,
                (dialog, which) -> {
                    String newProjectName = projectNameEditText.getText().toString().trim();

                    if (newProjectName.isEmpty()) {
                        showError(errorMessageTextView, R.string.project_name_empty);
                        return RETURN;
                    }

                    if (isProjectNameExists(newProjectName)) {
                        showError(errorMessageTextView, R.string.project_exists);
                        return RETURN;
                    }

                    ProjectManager.renameProject(oldProjectName, newProjectName);
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

    private static void showError(TextView errorView, int resId) {
        errorView.setText(resId);
        errorView.setVisibility(View.VISIBLE);
    }
}
