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
import java.io.IOException;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeAdapter;
import org.xedox.webaide.R;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.git.GitManager;
import org.xedox.webaide.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class GitCommitDialog {
    public static void show(BaseActivity context, GitManager git) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_commit);
        builder.setView(R.layout.git_commit_dialog);
        TextInputEditText nameF = builder.findViewById(R.id.name);
        TextView errorTextView = builder.findViewById(R.id.error_message);
        nameF.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.git_commit,
                (dialog, which) -> {
                    String name = nameF.getText().toString();
                    if (name.isBlank()) {
                        errorTextView.setText(R.string.git_commit_name_cannot_be_empty);
                        errorTextView.setVisibility(View.VISIBLE);
                        return RETURN;
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                    ConsoleLayout console = null;
                    if (context instanceof EditorActivity) {
                        console = ((EditorActivity) context).findViewById(R.id.console_layout);
                    }
                    try {
                        git.commit(name);
                        console.printError(R.string.git_commit_successful);
                    } catch (Exception err) {
                        err.printStackTrace();
                        console.printError(R.string.git_commit_failed);
                    }
                    return EXIT;
                });

        builder.show();
    }
}
