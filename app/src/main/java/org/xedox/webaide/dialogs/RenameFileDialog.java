package org.xedox.webaide.dialogs;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeAdapter;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.R;

import java.io.File;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class RenameFileDialog {
    private static final String TAG = "RenameFileDialog";

    public static void show(BaseActivity context, FileTreeAdapter adapter, Node node) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.rename_file);
        builder.setView(R.layout.rename_file_dialog);

        TextInputEditText nameInput = builder.findViewById(R.id.name);
        TextView errorTextView = builder.findViewById(R.id.error_message);

        nameInput.requestFocus();
        nameInput.post(() -> nameInput.selectAll());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.rename,
                (dialog, which) -> {
                    String fileName = nameInput.getText().toString();

                    if (fileName.trim().isEmpty()) {
                        errorTextView.setText(R.string.project_name_empty);
                        errorTextView.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (new File(node.path, fileName).exists()) {
                        errorTextView.setText(R.string.project_exists);
                        errorTextView.setVisibility(View.VISIBLE);
                        return;
                    }

                    File oldFile = new File(node.fullPath);
                    File newFile = new File(node.path, fileName);

                    if (oldFile.renameTo(newFile)) {
                        node.name = fileName;
                        adapter.renameNode(node, fileName);
                        dialog.dismiss();
                    } else {
                        errorTextView.setText(R.string.rename_failed);
                        errorTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                });

        builder.show();
    }
}
