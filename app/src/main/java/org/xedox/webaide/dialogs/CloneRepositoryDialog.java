package org.xedox.webaide;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.project.ProjectsAdapter;
import org.xedox.webaide.util.GitManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloneRepositoryDialog {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final BaseActivity context;
    private final ProjectsAdapter adapter;
    private final DialogBuilder dialogBuilder;
    private final TextView errorMessage;
    private final ProgressBar progressBar;

    public CloneRepositoryDialog(BaseActivity context, ProjectsAdapter adapter) {
        this.context = context;
        this.adapter = adapter;

        this.dialogBuilder =
                new DialogBuilder(context)
                        .setTitle(R.string.git_clone)
                        .setView(R.layout.dialog_input)
                        .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                        .setPositiveButton(R.string.clone, (d, which) -> processClone());

        this.errorMessage = dialogBuilder.findViewById(R.id.error_message);
        this.progressBar = dialogBuilder.findViewById(R.id.progress); // fix: ambil progressBar dari dialog, bukan activity
    }

    public void show() {
        dialogBuilder.show();
    }

    private void processClone() {
        TextInputEditText urlInput = dialogBuilder.findViewById(R.id.input);
        String url = urlInput.getText().toString().trim();

        if (url.isEmpty()) {
            showError(R.string.git_clone_url_empty);
            return;
        }

        cloneRepository(url);
    }

    private void cloneRepository(String repoUrl) {
        showProgress(true);

        executor.execute(
                () -> {
                    try {
                        GitManager.clone(context, repoUrl, IDE.PROJECTS_PATH.getAbsolutePath());
                        handleCloneSuccess(repoUrl);
                    } catch (Exception e) {
                        handleCloneFailure(repoUrl);
                    } finally {
                        showProgress(false);
                    }
                });
    }

    private void handleCloneSuccess(String repoUrl) {
        mainHandler.post(
                () -> {
                    context.showSnackbar(R.string.git_clone_successful);
                    String repoName = GitManager.extractRepoNameFromUrl(repoUrl);
                    adapter.add(new Project(repoName));
                });
    }

    private void handleCloneFailure(String repoUrl) {
        mainHandler.post(
                () -> context.showSnackbar(context.getString(R.string.git_clone_failed, repoUrl)));
    }

    private void showProgress(boolean show) {
        mainHandler.post(
                () -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showError(int errorResId) {
        mainHandler.post(
                () -> {
                    errorMessage.setText(errorResId);
                    errorMessage.setVisibility(View.VISIBLE);
                });
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void show(BaseActivity context, ProjectsAdapter adapter) {
        new CloneRepositoryDialog(context, adapter).show(); // FIXED: tambahkan .show()
    }
}
