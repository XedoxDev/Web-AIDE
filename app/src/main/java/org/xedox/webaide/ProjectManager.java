package org.xedox.webaide;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.io.Assets;
import org.xedox.webaide.io.FileX;
import static org.xedox.webaide.IDE.*;
import org.xedox.webaide.io.IFile;

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
        if (file.exists()) activity.showSnackbar(R.string.project_exists);
        file.mkdirs();

        IFile index = new FileX(file, "index.html");
        index.mkfile();
        try {
            index.write(Assets.from(activity).asset("base_index.html").read());
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
}
