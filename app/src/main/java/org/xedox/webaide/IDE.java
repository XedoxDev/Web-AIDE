package org.xedox.webaide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import android.webkit.MimeTypeMap;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import java.io.File;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.dialogs.DialogBuilder;

public class IDE extends MultiDexApplication {

    public static File HOME;
    public static File PROJECTS_PATH;
    public static Context context;
    public static int TAB_SIZE = 4;
    private static boolean isInit = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static void init(BaseActivity activity) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        TAB_SIZE = getTabSize(pref);
        initDialogType(pref);
        if (isInit) return;
        HOME = activity.getExternalFilesDir(null);
        PROJECTS_PATH = new File(HOME, "Projects");
        if (!PROJECTS_PATH.exists()) {
            PROJECTS_PATH.mkdirs();
        }

        isInit = true;
    }

    private static void initDialogType(SharedPreferences pref) {
        try {
            String dialogType =
                    pref.getString("dialog_type", "androidx.appcompat.app.AlertDialog.Builder");
            Class<?> clazz = Class.forName(dialogType);

            if (AlertDialog.Builder.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends AlertDialog.Builder> clazzBuilder =
                        (Class<? extends AlertDialog.Builder>) clazz;
                DialogBuilder.builderType = clazzBuilder;
            } else {
                DialogBuilder.builderType = AlertDialog.Builder.class;
            }
        } catch (ClassNotFoundException e) {
            DialogBuilder.builderType = AlertDialog.Builder.class;
            e.printStackTrace();
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
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void openLinkInBrowser(Activity activity, String link) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFileInExternalApp(Context context, File file) {
        try {
            Uri uri =
                    FileProvider.getUriForFile(
                            context, context.getPackageName() + ".provider", file);

            String mime = getMimeType(file.getPath());
            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, mime)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMimeType(String url) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        return ext != null
                ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                : "*/*";
    }
}
