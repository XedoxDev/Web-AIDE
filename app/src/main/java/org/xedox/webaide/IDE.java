package org.xedox.webaide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import android.webkit.MimeTypeMap;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.dialogs.ErrorDialog;
import org.xedox.webaide.editor.SoraEditorManager;
import org.xedox.webaide.editor.format.IFormatter;
import org.xedox.webaide.util.AlertDialogBuilderX;

public class IDE extends MultiDexApplication {

    public static File HOME;
    public static File PROJECTS_PATH;
    public static IDE instance;

    private static boolean isInit = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static void init(BaseActivity activity) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        try {
            if (!isInit) {
                String themePref = pref.getString("theme", "system");
                HOME = activity.getExternalFilesDir(null);
                PROJECTS_PATH = new File(HOME, "Projects");
                if (!PROJECTS_PATH.exists()) {
                    PROJECTS_PATH.mkdirs();
                }
                try {
                    SoraEditorManager.initSchemes(activity);
                } catch (Exception err) {
                    err.printStackTrace();
                    ErrorDialog.show(activity, "Failed to init editor themes", err);
                }
                try {
                    applyTheme(activity, themePref);
                } catch (Exception err) {
                    err.printStackTrace();
                    ErrorDialog.show(activity, "Theme apply failed", err);
                }
                isInit = true;
            }
            IFormatter.updateTabSize(pref);
            initDialogType(pref);
        } catch (Throwable err) {
            err.printStackTrace();
            ErrorDialog.show(activity, "Failed to pre init activity", err);
        }
    }

    private static void initDialogType(SharedPreferences pref) {
        String dialogType = pref.getString("dialog_type", "AlertDialogX");
        switch (dialogType) {
            case "Material3":
                DialogBuilder.builderType = MaterialAlertDialogBuilder.class;
                break;
            case "AndroidX":
                DialogBuilder.builderType = AlertDialog.Builder.class;
                break;
            case "AlertDialogX":
            default:
                DialogBuilder.builderType = AlertDialogBuilderX.class;
                break;
        }
    }

    public static int getTabSize(SharedPreferences pref) {
        try {
            return Integer.parseInt(pref.getString("editor_tab_size", "4"));
        } catch (Exception e) {
            return 4;
        }
    }

    public static boolean isDarkMode(Context context) {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    public static void applyTheme(Context context, String themeValue) {
        switch (themeValue) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        String theme = SoraEditorManager.currentTheme + (isDarkMode(context) ? "_dark" : "_light");
        SoraEditorManager.changeEditorScheme(context, theme);
    }
}
