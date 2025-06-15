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
import org.xedox.webaide.util.GitManager;
import org.xedox.webaide.util.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class GitAddDialog {
    public static void show(BaseActivity context, GitManager git) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_add);
        builder.setView(R.layout.git_add_dialog);
        TextInputEditText nameF = builder.findViewById(R.id.pattern);

        nameF.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.git_add,
                (dialog, which) -> {
                    String name = nameF.getText().toString();
                    try {
                        git.add(name);
                        if (context instanceof EditorActivity) {
                            EditorActivity activity = (EditorActivity) context;
                            ConsoleLayout console = activity.findViewById(R.id.console_layout);
                            console.printText(R.string.git_add_successful);
                        }
                    } catch (Exception e) {
                        if (context instanceof EditorActivity) {
                            EditorActivity activity = (EditorActivity) context;
                            ConsoleLayout console = activity.findViewById(R.id.console_layout);
                            console.printError(R.string.git_add_failed, e);
                        }
                    }
                    dialog.dismiss();
                });

        builder.show();
    }
}
