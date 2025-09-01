package org.xedox.webaide;

import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.NoSuchElementException;
import org.xedox.utils.FileX;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.utils.dialog.NeoAlertDialogBuilder;
import org.xedox.utils.format.FormatConfig;
import org.xedox.utils.format.IFormat;
import org.xedox.webaide.sora.SoraEditorManager;
import static androidx.appcompat.app.AppCompatDelegate.*;
import org.xedox.webaide.dialog.CopyAssetsDialog;

public class AppCore extends MultiDexApplication {

    public static AppCore instance;

    private String filesDir;
    private String projectsDir;
    private String textmateDir;
    private String fontsDir;
    private String themesDir;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        filesDir = instance.getExternalFilesDir(null).getAbsolutePath() + "/";
        projectsDir = filesDir + "projects/";
        textmateDir = filesDir + "textmate/";
        fontsDir = filesDir + "fonts/";
        themesDir = textmateDir + "themes/";
        mkdirs(projectsDir, textmateDir, themesDir, fontsDir);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        AppCore.setAppDelegate(sp.getString("app_theme", "SYSTEM"));
        AppCore.setDialogType(sp.getString("app_dialog_type", "MATERIAL"));

        FormatConfig.getInstance().setUseTab(sp.getBoolean("editor_use_tab", false));
        FormatConfig.getInstance()
                .setIndentSize(Integer.parseInt(sp.getString("editor_indent_size", "4")));
        SoraEditorManager.font =
                sp.getString("editor_font", fontsDir + "SourceCodePro-Regular.ttf");
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
            case "textmate" -> instance.textmateDir;
            case "fonts" -> instance.fontsDir;
            case "themes" -> instance.themesDir;
            default -> throw new NoSuchElementException("No exists directory: " + name);
        };
    }

    public static String string(int id) {
        return instance.getString(id);
    }

    public static FileX file(String name) {
        return new FileX(dir(name));
    }

    public static File[] fontList() {
        return new File(dir("fonts")).listFiles();
    }

    public static void setAppDelegate(String newValue) {
        int mode = MODE_NIGHT_FOLLOW_SYSTEM;
        if ("DARK".equals(newValue)) {
            mode = MODE_NIGHT_YES;
        } else if ("LIGHT".equals(newValue)) {
            mode = MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void setDialogType(String type) {
        if ("ANDROIDX".equals(type)) {
            DialogBuilder.builderType = AlertDialog.Builder.class;
        } else if ("NEO".equals(type)) {
            DialogBuilder.builderType = NeoAlertDialogBuilder.class;
        } else {
            DialogBuilder.builderType = MaterialAlertDialogBuilder.class;
        }
    }
}
