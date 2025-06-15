package org.xedox.webaide.activity.editor;

import org.xedox.webaide.R;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.dialogs.*;
import org.xedox.webaide.util.GitManager;
import android.view.MenuItem;

public class EditorGitManager {
    private final GitManager git;
    private final EditorActivity activity;
    private final ConsoleLayout console;
    private boolean initialized = false;

    public EditorGitManager(EditorActivity activity, GitManager git, ConsoleLayout console) {
        this.activity = activity;
        this.console = console;
        this.git = git;

        if (git != null) {
            try {
                this.initialized = git.isInitialized();
            } catch (Exception e) {
                console.printError(R.string.git_check_failed, e);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (git == null) {
            console.printError(R.string.git_manager_null);
            return false;
        }

        int id = item.getItemId();

        try {
            if (id == R.id.git_init) {
                handleGitInit();
                return true;
            } else if (id == R.id.git_add) {
                return handleGitAdd();
            } else if (id == R.id.git_auth) {
                handleGitAuth();
                return true;
            } else if (id == R.id.git_push) {
                return handleGitPush();
            } else if (id == R.id.git_status) {
                return handleGitStatus();
            } else if (id == R.id.git_remote) {
                handleGitRemote();
                return true;
            } else if (id == R.id.git_pull) {
                return handleGitPull();
            } else if (id == R.id.git_commit) {
                handleGitCommit();
                return true;
            }
        } catch (Exception e) {
            console.printError(R.string.git_operation_failed, e);
        }
        return false;
    }

    private void handleGitRemote() {
        GitRemoteDialog.show(activity, git);
    }

    private void handleGitCommit() {
        GitCommitDialog.show(activity, git);
    }

    private void handleGitInit() {
        try {
            git.init();
            initialized = true;
            console.printText(R.string.git_init_successful);
        } catch (Exception e) {
            initialized = false;
            console.printError(R.string.git_init_failed, e);
        }
    }

    private boolean handleGitPush() {
        if (!checkGitReady()) return false;

        try {
            git.push();
            console.printText(R.string.git_push_successful);
            return true;
        } catch (Exception e) {
            console.printError(R.string.git_push_failed, e);
            return false;
        }
    }

    private boolean handleGitStatus() {
        if (!checkGitReady()) return false;

        GitStatusDialog.show(activity, git);
        return true;
    }

    private boolean handleGitPull() {
        if (!checkGitReady()) return false;
        try {
            git.pull();
            console.printText(R.string.git_pull_successful);
        } catch (Exception err) {
            err.printStackTrace();
            console.printText(R.string.git_pull_failed);
        }

        return true;
    }

    private boolean handleGitAdd() {
        if (!checkGitReady()) return false;

        GitAddDialog.show(activity, git);
        return true;
    }

    private void handleGitAuth() {
        GitAuthDialog.show(activity, git);
    }

    private boolean checkGitReady() {
        if (git == null) {
            console.printError(R.string.git_manager_null);
            return false;
        }

        if (!initialized) {
            console.printError(R.string.git_not_initialized);
            return false;
        }

        return true;
    }
}
