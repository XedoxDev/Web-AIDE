package org.xedox.webaide.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.xedox.webaide.IDE;
import org.xedox.webaide.R;
import org.xedox.webaide.project.ProjectManager;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class RenameProjectDialog {
    private static final String TAG = "RenameProjectDialog";

    public static void show(Context context, String oldProjectName, int projectPosition) {

        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.rename_project);
        builder.setView(R.layout.dialog_input);

        TextInputEditText projectNameEditText = builder.findViewById(R.id.input);
        TextView errorMessageTextView = builder.findViewById(R.id.error_message);

        projectNameEditText.setText(oldProjectName);
        projectNameEditText.requestFocus();
        projectNameEditText.post(() -> projectNameEditText.selectAll());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.rename,
                (dialog, which) -> {
                    String newProjectName = projectNameEditText.getText().toString().trim();

                    if (newProjectName.isEmpty()) {
                        showError(errorMessageTextView, R.string.project_name_empty);
                        return;
                    }

                    if (isProjectNameExists(newProjectName)) {
                        showError(errorMessageTextView, R.string.project_exists);
                        return;
                    }

                    ProjectManager.renameProject(oldProjectName, newProjectName);
                    dialog.dismiss();
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
