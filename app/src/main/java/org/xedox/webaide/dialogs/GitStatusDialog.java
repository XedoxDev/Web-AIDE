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

public class GitStatusDialog {
    public static void show(BaseActivity context, GitManager git) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_status);
        ConsoleLayout console = null;
        if (context instanceof EditorActivity) {
            EditorActivity activity = (EditorActivity) context;
            console = activity.findViewById(R.id.console_layout);
        }

        try {
            builder.setMessage(git.getFullStatus());
        } catch (Exception err) {
            if (console != null) console.printError(err.toString());
        }

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
