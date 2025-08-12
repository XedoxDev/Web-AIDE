package org.xedox.webaide;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import java.io.File;
import java.util.NoSuchElementException;
import org.xedox.utils.FileX;
import org.xedox.webaide.editor.sora.SoraEditorManager;
import static androidx.appcompat.app.AppCompatDelegate.*;

public class AppCore extends MultiDexApplication {

    public static AppCore instance;

    private String filesDir;
    private String projectsDir;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        filesDir = getExternalFilesDir(null).getAbsolutePath() + "/";
        projectsDir = filesDir + "projects/";
        mkdirs(projectsDir);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        AppCore.setAppDelegate(sp.getString("app_theme", "SYSTEM"));
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

    public static void setAppDelegate(String newValue) {
        int mode = MODE_NIGHT_FOLLOW_SYSTEM;
        if("DARK".equals(newValue)) {
             mode = MODE_NIGHT_YES;
        } else if("LIGHT".equals(newValue)) {
             mode = MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
