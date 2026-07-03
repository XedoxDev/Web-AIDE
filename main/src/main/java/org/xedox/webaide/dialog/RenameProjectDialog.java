package org.xedox.webaide.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.webaide.R;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectsAdapter;

public class RenameProjectDialog {

    public static void show(Context context, ProjectsAdapter projectsAdapter, Project project) {

        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_input_layout);
        builder.setTitle(R.string.rename_project);
        builder.setCancelable(false);

        TextInputEditText inputView = builder.findViewById(R.id.input);
        TextView errorMessage = builder.findViewById(R.id.errorMessage);

        inputView.setHint(R.string.project_name);

        // Isi nama project lama
        inputView.setText(project.getName());
        inputView.setSelection(project.getName().length());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(R.string.rename, (dialog, which) -> {

            String input = inputView.getText().toString().trim();

            errorMessage.setVisibility(View.GONE);

            if (input.isEmpty()) {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.project_name_cannot_be_empty);
                return;
            }

            // Jika nama tidak berubah, langsung tutup dialog
            if (input.equals(project.getName())) {
                dialog.dismiss();
                return;
            }

            if (Project.existsProject(input)) {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.project_already_exists);
                return;
            }

            try {
                Project.renameProject(project, input);
projectsAdapter.reload();
dialog.dismiss();
            } catch (Exception e) {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(e.getMessage());
            }
        });

        builder.show();
    }
}
