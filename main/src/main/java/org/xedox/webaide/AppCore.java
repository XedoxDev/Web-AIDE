package org.xedox.webaide;

import androidx.multidex.MultiDexApplication;
import java.io.File;
import java.util.NoSuchElementException;
import org.xedox.utils.FileX;

public class AppCore extends MultiDexApplication {

    public static AppCore instance;

    private String filesDir;
    private String projectsDir;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        filesDir = "/data/data/org.xedox.webaide/files/";
        projectsDir = filesDir + "projects/";
        mkdirs(projectsDir);
    }

    private void mkdirs(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static String dir(String name) {
        return switch (name) {
            case "files" -> instance.filesDir;
            case "projects" -> instance.projectsDir;
            default -> throw new NoSuchElementException("No exists directory: " + name);
        };
    }
    
    public static String string(int id) {
    	return instance.getString(id);
    }

    public static FileX file(String name) {
        return new FileX(dir(name));
    }
}
