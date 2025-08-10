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

public class CreateProjectDialog {
    public static void show(BaseActivity context, ProjectsAdapter projectsAdapter) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_input_layout);
        builder.setTitle(R.string.create_project);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        TextInputEditText inputView = builder.findViewById(R.id.input);
        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        inputView.setHint(R.string.project_name);

        builder.setPositiveButton(
                R.string.create_project,
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
                        Project project = Project.createProject(context, input);
                        context.showSnackbar(R.string.project_successful_created, Snackbar.LENGTH_SHORT);
                        projectsAdapter.add(0, project);
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
