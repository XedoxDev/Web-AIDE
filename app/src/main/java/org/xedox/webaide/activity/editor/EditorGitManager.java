package org.xedox.webaide.activity.editor;

import org.xedox.webaide.R;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.dialogs.GitRemoteDialog;
import org.xedox.webaide.dialogs.GitStatusDialog;
import org.xedox.webaide.dialogs.GitAddDialog;
import org.xedox.webaide.dialogs.GitAuthDialog;
import org.xedox.webaide.git.GitManager;
import android.view.MenuItem;

public class EditorGitManager {
    private GitManager git;
    private EditorActivity activity;
    private ConsoleLayout console;

    public EditorGitManager(EditorActivity activity, GitManager git, ConsoleLayout console) {
        this.git = git;
        this.activity = activity;
        this.console = console;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.git_init) {
            handleGitInit();
            return true;
        } else if (id == R.id.git_add) {
            handleGitAdd();
            return true;
        } else if (id == R.id.git_auth) {
            handleGitAuth();
            return true;
        } else if (id == R.id.git_push) {
            handleGitPush();
            return true;
        } else if (id == R.id.git_status) {
            handleGitStatus();
            return true;
        } else if (id == R.id.git_remote) {
            handleGitRemote();
            return true;
        } else {
            return false;
        }
    }

    private void handleGitRemote() {
        GitRemoteDialog.show(activity, git);
    }

    private void handleGitInit() {
        try {
            git.init();
            console.printText(R.string.git_init_successful);
        } catch (Exception e) {
            console.printError(R.string.git_init_failed, e);
        }
    }

    private void handleGitPush() {
        try {
            git.push();
            console.printText(R.string.git_push_successful);
        } catch (Exception e) {
            console.printError(R.string.git_push_failed, e);
        }
    }

    private void handleGitStatus() {
        GitStatusDialog.show(activity, git);
    }

    private void handleGitPull() {
        try {
            git.pull();
            console.printText(R.string.git_pull_successful);
        } catch (Exception e) {
            console.printError(R.string.git_pull_failed, e);
        }
    }

    private void handleGitAdd() {
        GitAddDialog.show(activity, git);
    }

    private void handleGitAuth() {
        GitAuthDialog.show(activity, git);
    }
}
