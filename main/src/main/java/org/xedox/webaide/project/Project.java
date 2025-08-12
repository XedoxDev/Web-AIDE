package org.xedox.webaide.project;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.xedox.utils.Assets;
import org.xedox.utils.FileX;
import org.xedox.webaide.AppCore;

public class Project {
    private final FileX projectPath;

    public Project(File projectPath) {
        this.projectPath = new FileX(Objects.requireNonNull(projectPath));
    }

    public Project(String projectPath) {
        this(new File(Objects.requireNonNull(projectPath)));
    }

    public String getName() {
        return projectPath.getName();
    }

    public String getParent() {
        return projectPath.getParent();
    }

    public String getAbsolutePath() {
        return projectPath.getAbsolutePath();
    }

    public FileX getFile() {
        return projectPath;
    }

    private static File[] getProjectsFiles() {
        File projectsDir = new File(AppCore.dir("projects"));
        return projectsDir.exists() && projectsDir.isDirectory() ? projectsDir.listFiles() : null;
    }

    public static List<Project> getProjectsList() {
        List<Project> projects = new ArrayList<>();
        File[] files = getProjectsFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    projects.add(new Project(file));
                }
            }
            projects.sort(
                    (p1, p2) ->
                            Long.compare(p2.getFile().lastModified(), p1.getFile().lastModified()));
        }
        return projects;
    }

    public static boolean existsProject(String name) {
        if (name == null || name.isEmpty()) return false;

        File[] files = getProjectsFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.isDirectory() && file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static Project createProject(Context context, String name) throws IOException {
        File projectsDir = new File(AppCore.dir("projects"));
        File projectDir = new File(projectsDir, name);
        projectDir.mkdirs();
        try (Assets ass = Assets.from(context)) {
            ass.copyFile("project/index.html", new File(projectDir, "index.html"));
        } catch (Exception err) {
            err.printStackTrace();
        }
        return new Project(projectDir);
    }
    
    public String file(String path) {
    	return new File(projectPath, path).getAbsolutePath();
    }

    public static void renameProject(Project project, String newName) {
        File newFile = new File(project.getParent(), newName);
        new File(project.getAbsolutePath()).renameTo(newFile);
    }

    public static void removeProject(Project project) {
        project.projectPath.deleteRecursively();
    }

    public static Project getProject(String name) {
        return new Project(new FileX(AppCore.dir("projects"), name));
    }
}
