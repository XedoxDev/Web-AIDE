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

public class GitAuthDialog {
    public static void show(BaseActivity context, GitManager git) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_auth);
        builder.setView(R.layout.git_auth_dialog);
        TextInputEditText loginF = builder.findViewById(R.id.login);
        TextInputEditText passwordF = builder.findViewById(R.id.password);
        TextView errorTextView = builder.findViewById(R.id.error_message);

        loginF.requestFocus();
        loginF.setText(GitManager.getSavedLogin(context));
        passwordF.setText(GitManager.getSavedToken(context));
        loginF.setSelection(0, loginF.getText().length());
        
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(
                R.string.login,
                (dialog, which) -> {
                    String login = loginF.getText().toString();
                    String password = passwordF.getText().toString();
                    errorTextView.setVisibility(View.VISIBLE);
                    if (login.isBlank() || password.isBlank()) {
                        errorTextView.setText(R.string.login_or_password_cannot_be_empty);
                        errorTextView.setVisibility(View.VISIBLE);
                        return RETURN;
                    }
                    git.setCredentials(login, password);
                    if (context instanceof EditorActivity) {
                        ConsoleLayout console =
                                ((EditorActivity) context).findViewById(R.id.console_layout);
                        console.printText(R.string.git_auth_successful);
                    }
                    return EXIT;
                });

        builder.show();
    }
}
