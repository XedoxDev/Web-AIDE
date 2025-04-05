package org.xedox.webaide;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.adapters.ProjectsAdapter;
import java.util.concurrent.ExecutorService;
import android.os.Handler;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.console.ConsoleLayout;
import java.util.concurrent.Executors;
import android.os.Looper;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.view.View;
import android.widget.ProgressBar;
import org.xedox.webaide.git.GitManager;
import android.util.Log;
import org.xedox.webaide.R;
import org.xedox.webaide.io.FileX;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class CloneRepositoryDialog {
    private final BaseActivity context;
    private final ProjectsAdapter adapter;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CloneRepositoryDialog(BaseActivity context, ProjectsAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    public void show() {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.git_clone);
        builder.setView(R.layout.clone_repository_dialog);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);
        builder.setPositiveButton(R.string.clone, (dialog, which) -> handleClone(builder));
        builder.show();
    }

    public static void showStatic(BaseActivity context, ProjectsAdapter adapter) {
        new CloneRepositoryDialog(context, adapter).show();
    }

    private boolean handleClone(DialogBuilder builder) {
        TextInputEditText urlEditText = builder.findViewById(R.id.name);
        String url = urlEditText.getText().toString().trim();

        if (url.isEmpty()) {
            showError(builder, R.string.git_clone_url_empty);
            return RETURN;
        }
        cloneRepository(url);
        return EXIT;
    }

    private void cloneRepository(String repoUrl) {
        ProgressBar progressBar = context.findViewById(R.id.progress);
        ConsoleLayout consoleLayout = context.findViewById(R.id.console_layout);

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        service.execute(
                () -> {
                    try {
                        GitManager.clone(repoUrl, IDE.PROJECTS_PATH.getAbsolutePath());
                        context.showSnackbar(R.string.git_clone_successful);
                        handler.post(
                                () -> {
                                    adapter.add(
                                            new Project(
                                                    new FileX(
                                                                    IDE.PROJECTS_PATH,
                                                                    GitManager.extractRepoNameFromUrl(repoUrl))
                                                            .getAbsolutePath()));
                                });
                    } catch (Exception err) {
                        context.showSnackbar(context.getString(R.string.git_clone_failed, repoUrl));
                        printError(consoleLayout, R.string.git_clone_failed, err);
                    } finally {
                        handler.post(
                                () -> {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                });
    }

    private void printError(ConsoleLayout console, int errorResId, Throwable e) {
        if (console != null) {
            handler.post(() -> console.printError(errorResId, e));
        }
    }

    private void showError(DialogBuilder builder, int errorResId) {
        TextView error = builder.findViewById(R.id.error_message);
        error.setText(errorResId);
        error.setVisibility(View.VISIBLE);
    }

    public void shutdown() {
        service.shutdown();
    }
}
