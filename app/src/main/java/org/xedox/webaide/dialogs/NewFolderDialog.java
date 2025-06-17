package org.xedox.webaide.dialogs;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeAdapter;
import org.xedox.webaide.R;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.util.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class NewFolderDialog {

    private static final String TAG = "NewFolderDialog";

    public static void show(BaseActivity context, FileTreeAdapter adapter, Node parent) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.create_new_folder);
        builder.setView(R.layout.dialog_input);

        TextInputEditText folderNameEditText = builder.findViewById(R.id.input);
        TextView errorMessageTextView = builder.findViewById(R.id.error_message);

        folderNameEditText.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.create,
                (dialog, which) -> {
                    String folderName = folderNameEditText.getText().toString().trim();
                    File folder = new File(parent.fullPath, folderName);

                    if (folderName.isEmpty()) {
                        errorMessageTextView.setText(R.string.folder_name_empty);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (folder.exists()) {
                        errorMessageTextView.setText(R.string.folder_exists);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (folder.mkdirs()) {
                        Node newNode = new Node(folder.getAbsolutePath());
                        if (parent.isOpen) adapter.addNode(parent, newNode);
                        dialog.dismiss();
                    } else {
                        errorMessageTextView.setText(R.string.folder_creation_failed);
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                });

        builder.show();
    }
}
