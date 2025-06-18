package org.xedox.webaide.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.util.io.FileX;
import static org.xedox.webaide.IDE.*;
import org.xedox.webaide.util.io.IFile;
import org.xedox.webaide.R;

public class ProjectManager {

    public static List<Project> getProjects() {
        List<Project> projects = new ArrayList<>();
        for (File file : PROJECTS_PATH.listFiles()) {
            projects.add(new Project(file.getAbsolutePath()));
        }
        return projects;
    }

    public static Project createProject(String name, BaseActivity activity) {
        IFile file = new FileX(PROJECTS_PATH, name);
        try {
            IFile.unzip(activity.getAssets().open("project_example.zip"), file.getFullPath());
        } catch (Exception err) {
            err.printStackTrace();
        }
        activity.showSnackbar(R.string.project_created_successful);
        return new Project(file.getName());
    }

    public static void removeProject(String name) {
        IFile file = new FileX(PROJECTS_PATH, name);
        if (!file.exists()) return;
        if (file.isDir()) file.removeDir();
        else file.remove();
    }

    public static void renameProject(String original, String newName) {
        File orig = new File(PROJECTS_PATH, original);
        orig.renameTo(new File(PROJECTS_PATH, newName));
    }

    public static Project cloneProject(String originalName, BaseActivity activity) {
        File original = new File(PROJECTS_PATH, originalName);
        if (!original.exists() || !original.isDirectory()) return null;

        int cloneNumber = 1;
        File clone;
        do {
            clone = new File(PROJECTS_PATH, originalName + "_copy" + cloneNumber);
            cloneNumber++;
        } while (clone.exists());

        try {
            copyDirectory(original, clone);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        activity.showSnackbar(R.string.project_cloned_successful);
        return new Project(clone.getName());
    }

    private static void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) destination.mkdirs();
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(destination, child));
                }
            }
        } else {
            java.nio.file.Files.copy(
                source.toPath(),
                destination.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
