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
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class FileNotSavedDialog {

    private static final String TAG = "NewFolderDialog";

    public static void show(BaseActivity context) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.exit);
        builder.setMessage(R.string.files_not_saved);

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.ok,
                (dialog, which) -> {
                    if (context instanceof EditorActivity) {
                        EditorActivity activity = (EditorActivity) context;
                        activity.saveAllFiles();
                        activity.finish(false);
                    }
                    return EXIT;
                });

        builder.show();
    }
}
