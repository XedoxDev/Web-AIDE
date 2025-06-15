package org.xedox.webaide.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeAdapter;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.IDE;
import org.xedox.webaide.activity.MainActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectManager;
import org.xedox.webaide.project.ProjectsAdapter;
import org.xedox.webaide.util.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;
import org.xedox.webaide.util.io.IFile;

public class NewFileDialog {

    private static final String TAG = "NewProjectDialog";

    public static void show(BaseActivity context, FileTreeAdapter adapter, Node parent) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.create_new_file);
        builder.setView(R.layout.new_file_dialog);
        TextInputEditText projectNameEditText = builder.findViewById(R.id.name);
        TextView errorMessageTextView = builder.findViewById(R.id.error_message);

        projectNameEditText.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.create,
                (dialog, which) -> {
                    String fileName = projectNameEditText.getText().toString().trim();
                    if (fileName.isEmpty()) {
                        errorMessageTextView.setText(R.string.git_clone_url_empty);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    IFile file = new FileX(parent.fullPath, fileName);
                    file.mkfile();
                    if (parent.isOpen) adapter.addNode(parent, new Node(file.getFullPath()));

                    dialog.dismiss();
                });

        builder.show();
    }
}
