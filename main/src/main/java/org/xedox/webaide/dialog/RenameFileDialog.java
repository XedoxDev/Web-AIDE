package org.xedox.webaide.dialog;

import android.content.Context;
import java.io.File;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.view.View;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.webaide.R;

public class RenameFileDialog {
    public static void show(Context context, FileTreeView fileTree, Node node) {
        if (context == null || fileTree == null || node == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_input_layout);
        int title = node.isDirectory() ? R.string.rename_folder : R.string.rename_file;
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        TextInputEditText inputView = builder.findViewById(R.id.input);
        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        int hint = node.isDirectory() ? R.string.folder_name : R.string.file_name;
        inputView.setHint(hint);

        String currentName = node.getName();
        inputView.requestFocus();
        inputView.setText(currentName);
        int dot = currentName.lastIndexOf(".");
        if(dot != -1)
        inputView.setSelection(dot-1);
        else inputView.setSelection(currentName.length());
        builder.setPositiveButton(
                R.string.rename,
                (dialog, w) -> {
                    String input = inputView.getText().toString().trim();
                    Node newFile = new Node(node.getParent(), input);
                    newFile.setLevel(node.getLevel());
                    errorMessage.setVisibility(View.GONE);
                    if (input.isEmpty()) {
                        errorMessage.setVisibility(View.VISIBLE);
                        int err =
                                node.isDirectory()
                                        ? R.string.folder_name_cannot_be_empty
                                        : R.string.file_name_cannot_be_empty;
                        errorMessage.setText(err);
                        return;
                    }

                    if (newFile.exists()) {
                        errorMessage.setVisibility(View.VISIBLE);
                        int err =
                                node.isDirectory()
                                        ? R.string.folder_already_exists
                                        : R.string.file_already_exists;
                        errorMessage.setText(err);
                        return;
                    }

                    if (!isValidFilename(input)) {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.invalid_filename);
                        return;
                    }

                    try {
                        node.renameTo(newFile);
                        fileTree.updateNode(node, newFile);
                        dialog.dismiss();
                    } catch (Exception err) {
                        err.printStackTrace();
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(err.getMessage());
                    }
                });
        builder.show();
    }

    private static boolean isValidFilename(String name) {
        return !name.contains("/")
                && !name.contains("\\")
                && !name.contains(":")
                && !name.contains("*")
                && !name.contains("?")
                && !name.contains("\"")
                && !name.contains("<")
                && !name.contains(">")
                && !name.contains("|");
    }
}
