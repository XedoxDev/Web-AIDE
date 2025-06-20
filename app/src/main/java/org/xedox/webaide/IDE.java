package org.xedox.webaide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import android.webkit.MimeTypeMap;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import java.io.InputStream;
import org.eclipse.tm4e.core.registry.IThemeSource;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import java.io.File;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.dialogs.DialogBuilder;

public class IDE extends MultiDexApplication {

    public static File HOME;
    public static File PROJECTS_PATH;
    public static int TAB_SIZE = 4;
    private static boolean isInit = false;
    private static IDE instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPref.getString("theme", "system");
        applyTheme(themePref);
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

        try {
            GrammarRegistry.getInstance().loadGrammars("textmate/langs.json");
        } catch (Throwable e) {
            Log.e("IDE", "Failed to load grammars", e);
            activity.showSnackbar("Failed to load syntax grammars");
        }

        isInit = true;
    }

    private static void initDialogType(SharedPreferences pref) {
            String dialogType = pref.getString("dialog_type", "Material3");
            switch(dialogType) {
                case "Material3": DialogBuilder.builderType = MaterialAlertDialogBuilder.class; break;
                case "AndroidX":
                default:  DialogBuilder.builderType = AlertDialog.Builder.class;
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
            Log.e("IDE", "Error opening link", e);
        }
    }

    public static void openFileInExternalApp(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context, context.getPackageName() + ".provider", file);

            String mime = getMimeType(file.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, mime)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("IDE", "Error opening file", e);
        }
    }

    public static void changeEditorScheme(Context context) {
        if (context == null) return;

        FileProviderRegistry.getInstance()
                .addFileProvider(new AssetsFileResolver(context.getAssets()));

        FileProviderRegistry fileProviderRegistry = FileProviderRegistry.getInstance();
        String theme = isDarkMode(context) ? "darcula" : "darcula_light";
        String themePath = String.format("textmate/schemes/%s.json", theme);

        try (InputStream is = fileProviderRegistry.tryGetInputStream(themePath)) {
            if (is == null) {
                Log.e("IDE", "Failed to load theme: " + themePath);
                return;
            }

            IThemeSource source = IThemeSource.fromInputStream(is, themePath, null);
            ThemeModel model = new ThemeModel(source, theme);
            model.setDark(isDarkMode(context));

            ThemeRegistry.getInstance().loadTheme(model);
            ThemeRegistry.getInstance().setTheme(theme);
        } catch (Exception err) {
            Log.e("IDE", "Error applying editor theme", err);
        }
    }

    public static void applyTheme(String themeValue) {
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

        if (instance != null) {
            changeEditorScheme(instance);
        }
    }

    private static String getMimeType(String url) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        return ext != null
                ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                : "*/*";
    }
}