package org.xedox.webaide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.webkit.MimeTypeMap;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import org.eclipse.tm4e.core.registry.IThemeSource;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import java.io.File;
import org.xedox.webaide.activity.BaseActivity;
import static org.xedox.webaide.project.ProjectManager.*;

public class IDE extends MultiDexApplication {

    public static File HOME;
    public static File PROJECTS_PATH;
    public static Context context;

    public static int TAB_SIZE = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    private static boolean isInit = false;

    public static void init(BaseActivity activity) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(activity);
        String tabSizeStr = pref.getString("editor_tab_size", "4");
        TAB_SIZE = Integer.parseInt(tabSizeStr);
        if (isInit) return;
        Context context = activity.getApplicationContext();
        HOME = context.getExternalFilesDir(null);
        HOME.mkdirs();
        PROJECTS_PATH = new File(HOME, "Projects");
        if (!PROJECTS_PATH.exists()) PROJECTS_PATH.mkdirs();
        try {
            initSchemes(context);
        } catch (Throwable e) {
            activity.showSnackbar(e.toString());
        }
        isInit = true;
    }

    public static void initSchemes(Context context) {
        FileProviderRegistry.getInstance()
                .addFileProvider(new AssetsFileResolver(context.getAssets()));
        var themeRegistry = ThemeRegistry.getInstance();
        var name = "darcula"; // isDarkMode(context) ? "darcula" : "darculaLight";
        var themeAssetsPath = "textmate/schemes/" + name + ".json";
        var model =
                new ThemeModel(
                        IThemeSource.fromInputStream(
                                FileProviderRegistry.getInstance()
                                        .tryGetInputStream(themeAssetsPath),
                                themeAssetsPath,
                                null),
                        name);
        model.setDark(true);
        try {
            themeRegistry.loadTheme(model);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ThemeRegistry.getInstance().setTheme(name);
        GrammarRegistry.getInstance().loadGrammars("textmate/langs.json");
    }

    // https://t.me/AndroidIDE_Discussions/1/136733
    public static boolean isDarkMode(Context context) {
        int currentNightMode =
                context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void openLinkInBrowser(Activity activity, String link) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        activity.startActivity(i);
    }

    public static void openFileInExternalApp(Context context, File file) {
        Uri fileUri =
                FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        String mimeType = getMimeType(file.getPath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type != null ? type : "*/*";
    }
}
