package org.xedox.webaide.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.R;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectsAdapter;

public class RenameProjectDialog {
    public static void show(Context context, ProjectsAdapter projectsAdapter, Project project) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_input_layout);
        builder.setTitle(R.string.rename_project);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        TextInputEditText inputView = builder.findViewById(R.id.input);
        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        inputView.setHint(R.string.project_name);

        builder.setPositiveButton(
                R.string.rename,
                (dialog, w) -> {
                    String input = inputView.getText().toString().trim();
                    if (input.isBlank()) {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.project_name_cannot_be_empty);
                        return;
                    } else if (Project.existsProject(input)) {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.project_already_exists);
                        return;
                    }
                    try {
                        Project.renameProject(project, input);
                        projectsAdapter.update(project);
                        dialog.dismiss();
                    } catch (Exception err) {
                        err.printStackTrace();
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(err.getMessage());
                    }
                });
        builder.show();        
    }
}
