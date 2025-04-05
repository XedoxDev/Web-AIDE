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

import java.util.regex.Pattern;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeAdapter;
import org.xedox.webaide.R;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.git.GitManager;
import org.xedox.webaide.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class GitRemoteDialog {
    public static void show(BaseActivity context, GitManager git) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_clone_url);
        builder.setView(R.layout.git_remote_dialog);
        TextInputEditText urlF = builder.findViewById(R.id.url);
        TextView errorMessage = builder.findViewById(R.id.error_message);
        urlF.requestFocus();

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.git_add,
                (dialog, which) -> {
                    String url = urlF.getText().toString();
                    ConsoleLayout console = null;
                    if (context instanceof EditorActivity) {
                        console = ((EditorActivity) context).findViewById(R.id.console_layout);
                    }
                    if (isCorrectedLink(url)) {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.url_repo_not_corrected);
                    } else {
                        errorMessage.setVisibility(View.GONE);
                    }
                    try {
                        git.addRemote(url, "origin");
                        if(console!=null)console.printError(R.string.git_remote_successful);
                    } catch (Exception err) {
                        err.printStackTrace();
                        if(console!=null)console.printError(R.string.git_remote_failed, err);
                    }
                    return EXIT;
                });

        builder.show();
    }

    public static final String isCorrectedLinkRegex =
            "(https://)?(github\\.com/)([A-Za-z\\d]+)(\\.git)?";

    private static boolean isCorrectedLink(String source) {
        return Pattern.compile(isCorrectedLinkRegex).matcher(source).find();
    }
}
