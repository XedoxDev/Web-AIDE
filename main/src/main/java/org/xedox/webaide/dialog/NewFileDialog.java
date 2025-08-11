package org.xedox.webaide.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.io.File;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.webaide.project.ProjectsAdapter;
import org.xedox.webaide.project.Project;
import org.xedox.utils.dialog.DialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.view.View;
import org.xedox.webaide.R;

public class NewFileDialog {
    private static boolean isFolder = false;

    public static void show(Context context, FileTreeView fileTree, Node parent) {
        isFolder = false;
        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_new_file_layout);
        builder.setTitle(R.string.new_file_folder);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        TextInputEditText inputView = builder.findViewById(R.id.input);
        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        MaterialButtonToggleGroup buttonToggleGroup = builder.findViewById(R.id.create_type);
        inputView.setHint(R.string.file_name);
        buttonToggleGroup.check(R.id.file);
        inputView.requestFocus();
        buttonToggleGroup.addOnButtonCheckedListener(
                (view, id, checked) -> {
                    if (checked) {
                        if (id == R.id.file) {
                            inputView.setHint(R.string.file_name);
                            builder.dialog
                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setText(R.string.create_file);
                            inputView.setHint(R.string.file_name);
                        } else if (id == R.id.folder) {
                            inputView.setHint(R.string.file_name);
                            builder.dialog
                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setText(R.string.mkdir);
                            inputView.setHint(R.string.folder_name);
                        }
                    }
                });
        int ttl = isFolder ? R.string.mkdir : R.string.create_file; // title
        builder.setPositiveButton(
                ttl,
                (dialog, w) -> {
                    String input = inputView.getText().toString().trim();
                    File newFile = new File(parent, input);
                    if (input.isBlank()) {
                        errorMessage.setVisibility(View.VISIBLE);
                        int err =
                                isFolder
                                        ? R.string.folder_name_cannot_be_empty
                                        : R.string.file_name_cannot_be_empty;
                        errorMessage.setText(err);
                        return;
                    } else if (newFile.exists()) {
                        errorMessage.setVisibility(View.VISIBLE);
                        int err =
                                isFolder
                                        ? R.string.folder_already_exists
                                        : R.string.file_already_exists;
                        errorMessage.setText(err);
                        return;
                    }
                    try {
                        if (isFolder) {
                            if (newFile.mkdirs()) {
                                Node node = new Node(parent, input);
                                node.setLevel(parent.getLevel() + 1);
                                if (parent.isOpen())
                                    fileTree.addNode(fileTree.indexOfNode(parent) + 1, node);
                            }
                        } else {
                            if (newFile.createNewFile()) {
                                Node node = new Node(parent, input);
                                node.setLevel(parent.getLevel() + 1);
                                if (parent.isOpen())
                                    fileTree.addNode(fileTree.indexOfNode(parent) + 1, node);
                            }
                        }
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
