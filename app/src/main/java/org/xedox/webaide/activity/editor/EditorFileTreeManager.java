package org.xedox.webaide.activity.editor;

import android.view.View;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.webaide.project.Project;
import org.xedox.webaide.R;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.editor.EditorAdapter;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.IDE;
import org.xedox.webaide.util.OverflowMenu;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.dialogs.NewFileDialog;
import org.xedox.webaide.dialogs.NewFolderDialog;
import org.xedox.webaide.dialogs.RenameFileDialog;
import org.xedox.webaide.util.GitManager;
import org.xedox.webaide.util.io.FileX;
import org.xedox.webaide.util.io.IFile;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EditorFileTreeManager {
    private final EditorActivity context;
    private final FileTreeView fileTree;
    private final Project project;
    private final ExecutorService executor;
    private final EditorAdapter editorAdapter;
    private final ConsoleLayout console;
    private final GitManager git;

    public EditorFileTreeManager(
            EditorActivity context,
            FileTreeView fileTree,
            Project project,
            EditorAdapter editorAdapter,
            ConsoleLayout console,
            GitManager git) {
        this.context = context;
        this.fileTree = fileTree;
        this.project = project;
        this.editorAdapter = editorAdapter;
        this.console = console;
        this.git = git;
        this.executor = Executors.newSingleThreadExecutor();

        initialize();
    }

    private void initialize() {
        fileTree.adapter.setIcon(".md", R.drawable.markdown);
        fileTree.adapter.setOnFileClickListener((node, file, view) -> openFile(new FileX(file)));
        fileTree.adapter.setOnFileLongClickListener(this::onFileLongClick);
        fileTree.loadPath(new FileX(IDE.PROJECTS_PATH, project.name).getFullPath());
    }

    public boolean onFileLongClick(Node node, File file, View v) {
        int menuRes = node.isFile ? R.menu.file : R.menu.folder;
        OverflowMenu.show(
                context,
                v,
                menuRes,
                item -> {
                    handleFileOperation(node, item.getItemId());
                });
        return true;
    }

    private void handleFileOperation(Node node, int menuItemId) {
        if (menuItemId == R.id.remove) {
            showDeleteConfirmation(node);
        } else if (menuItemId == R.id.rename) {
            RenameFileDialog.show(context, fileTree.adapter, node);
        } else if (menuItemId == R.id.add_to_commit) {
            addToGitCommit(node);
        } else if (menuItemId == R.id.open_in_other_app) {
            openFileInExternalApp(node);
        } else {
            if (!node.isFile) {
                handleFolderOperation(node, menuItemId);
            }
        }
    }

    private void handleFolderOperation(Node node, int menuItemId) {
        if (menuItemId == R.id.new_file) {
            NewFileDialog.show(context, fileTree.adapter, node);
        } else if (menuItemId == R.id.new_folder) {
            NewFolderDialog.show(context, fileTree.adapter, node);
        } else if (menuItemId == R.id.add_file_from_external_storage) {
            importFileFromExternalStorage(node);
        }
    }

    private void importFileFromExternalStorage(Node node) {
        context.showFilePicker(
                (options) -> {
                    if (options == null
                            || options.length < 3
                            || options[1] == null
                            || options[2] == null) {
                        context.showSnackbar(R.string.file_import_failed);
                        return;
                    }

                    executor.execute(
                            () -> {
                                try {
                                    IFile destFile =
                                            new FileX(node.fullPath, options[1].toString());
                                    String content = options[2].toString();

                                    if (destFile.exists()) {
                                        context.runOnUiThread(
                                                () -> showOverwriteConfirmation(destFile, content));
                                    } else {
                                        writeFileWithFeedback(destFile, content);
                                    }
                                } catch (Exception e) {
                                    showErrorOnUiThread(R.string.file_import_error, e);
                                }
                            });
                });
    }

    private void showOverwriteConfirmation(IFile file, String content) {
        new DialogBuilder(context)
                .setTitle(R.string.file_exists)
                .setMessage(R.string.overwrite_file_confirmation)
                .setPositiveButton(
                        R.string.yes,
                        (d, w) -> {
                            executor.execute(() -> writeFileWithFeedback(file, content));
                            d.dismiss();
                        })
                .setNegativeButton(R.string.no, (d, w) -> d.dismiss())
                .show();
    }

    private void writeFileWithFeedback(IFile file, String content) {
        try {
            file.mkfile();
            file.write(content);
            context.runOnUiThread(
                    () -> {
                        fileTree.adapter.notifyDataSetChanged();
                        context.showSnackbar(R.string.file_import_success);
                    });
        } catch (Exception e) {
            showErrorOnUiThread(R.string.file_write_error, e);
        }
    }

    private void showDeleteConfirmation(Node node) {
        new DialogBuilder(context)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(context.getString(R.string.delete_confirmation, node.name))
                .setPositiveButton(
                        R.string.delete,
                        (d, w) -> {
                            deleteFile(node);
                            d.dismiss();
                        })
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .show();
    }

    private void deleteFile(Node node) {
        try {
            boolean deleted = new File(node.fullPath).delete();
            if (deleted) {
                fileTree.adapter.deleteNode(node);
                console.printText(R.string.file_deleted_success);
            } else {
                console.printError(R.string.file_delete_failed);
            }

        } catch (SecurityException e) {
            showErrorOnUiThread(R.string.file_delete_permission_denied, e);
        }
    }

    private void addToGitCommit(Node node) {
        try {
            git.add(node.fullPath);
            context.runOnUiThread(() -> console.printText(R.string.git_add_successful));
        } catch (Exception e) {
            showErrorOnUiThread(R.string.git_add_failed, e);
        }
    }

    private void openFileInExternalApp(Node node) {
        try {
            IDE.openFileInExternalApp(context, new File(node.fullPath));
        } catch (Exception e) {
            showErrorOnUiThread(R.string.file_open_external_error, e);
        }
    }

    public void openFile(IFile file) {
        if (executor.isShutdown()) return;
        int existingPosition = editorAdapter.findFilePosition(file);
        if (existingPosition >= 0) {
            context.getEditorPager().setCurrentItem(existingPosition, true);
        } else {
            editorAdapter.addFile(file);
            context.getEditorPager().setCurrentItem(editorAdapter.getItemCount() - 1, true);
        }
        context.updateMenu();
    }

    private void showErrorOnUiThread(int messageRes, Exception e) {
        context.runOnUiThread(
                () -> {
                    console.printError(messageRes, e);
                    context.showSnackbar(messageRes);
                });
    }

    public void shutdown() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            if (fileTree != null) {
                fileTree.shutdown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public FileTreeView getFileTreeView() {
        return fileTree;
    }
}
