package org.xedox.webaide.activity.editor;

import android.content.Context;
import android.view.View;

import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTree;
import org.xedox.webaide.Project;
import org.xedox.webaide.R;
import org.xedox.webaide.editor.EditorAdapter;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.IDE;
import org.xedox.webaide.OverflowMenu;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.dialogs.NewFileDialog;
import org.xedox.webaide.dialogs.NewFolderDialog;
import org.xedox.webaide.dialogs.RenameFileDialog;
import org.xedox.webaide.io.FileX;
import org.xedox.webaide.io.IFile;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorFileTreeManager {
    private final EditorActivity context;
    private final FileTree fileTree;
    private final Project project;
    private final ExecutorService executor;
    private final EditorAdapter editorAdapter;
    private final ConsoleLayout console;

    public EditorFileTreeManager(
            EditorActivity context,
            FileTree fileTree,
            Project project,
            EditorAdapter editorAdapter,
            ConsoleLayout console) {
        this.context = context;
        this.fileTree = fileTree;
        this.project = project;
        this.editorAdapter = editorAdapter;
        this.console = console;
        this.executor = Executors.newSingleThreadExecutor();

        initialize();
    }

    private void initialize() {
        fileTree.adapter.setOnFileClickListener((node, file, view) -> openFile(new FileX(file)));
        fileTree.adapter.setOnFileLongClickListener(this::onFileLongClick);
        fileTree.loadPath(new FileX(IDE.PROJECTS_PATH, project.name).getFullPath());
    }

    public boolean onFileLongClick(Node node, File file, View v) {
        int menuRes = node.isFile ? R.menu.file : R.menu.folder;
        OverflowMenu.show(context, v, menuRes, item -> handleFileOperation(node, item.getItemId()));
        return true;
    }

    private void handleFileOperation(Node node, int menuItemId) {
        if (menuItemId == R.id.remove) {
            deleteFile(node);
        } else if (menuItemId == R.id.rename) {
            RenameFileDialog.show(context, fileTree.adapter, node);
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
        }
    }

    private void deleteFile(Node node) {
        try {
            fileTree.adapter.deleteNode(node);
            boolean deleted = new File(node.fullPath).delete();
            if (!deleted) {
                console.printError(R.string.file_delete_failed);
            }
        } catch (SecurityException e) {
            console.printError(R.string.file_delete_permission_denied, e);
        }
    }

    public void openFile(IFile file) {
        executor.submit(
                () -> {
                    int existingPosition = editorAdapter.findFilePosition(file);
                    context.runOnUiThread(
                            () -> {
                                if (existingPosition >= 0) {
                                    context.getEditorPager().setCurrentItem(existingPosition, true);
                                } else {
                                    editorAdapter.addFile(file);
                                    context.getEditorPager()
                                            .setCurrentItem(editorAdapter.getItemCount() - 1, true);
                                }
                            });
                });
    }

    public void shutdown() {
        executor.shutdownNow();
        if (fileTree != null) {
            fileTree.shutdown();
        }
    }
}
