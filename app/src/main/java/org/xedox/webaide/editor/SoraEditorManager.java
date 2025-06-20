package org.xedox.webaide.editor;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.tm4e.core.registry.IThemeSource;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import org.xedox.webaide.dialogs.ErrorDialog;
import org.xedox.webaide.util.io.Assets;

public class SoraEditorManager {

    public static Set<String> themes;
    public static String currentTheme;

    public static void initSchemes(Context context) {
        try {
            themes = new HashSet<>();
            Gson gson = new Gson();
            JsonObject jsonObject =
                    gson.fromJson(
                            Assets.from(context).asset("textmate/themes.json").read(),
                            JsonObject.class);
            String[] themesArr = gson.fromJson(jsonObject.get("themes"), String[].class);
            for (String theme : themesArr) {
                themes.add(theme);
            }
            currentTheme = "darcula";
            for (String theme : themes) {
                loadScheme(context, theme + "_dark");
                loadScheme(context, theme + "_light");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            ErrorDialog.show(context, "Failed to default color themes", e);
        }
        try {
            GrammarRegistry.getInstance().loadGrammars("textmate/langs.json");
        } catch (Throwable e) {
            e.printStackTrace();
            ErrorDialog.show(context, "Failed to load editor languages", e);
        }
    }

    private static void loadScheme(Context context, String name) {
        try {
            AssetManager assets = context.getApplicationContext().getAssets();
            AssetsFileResolver assetsFileResolver = new AssetsFileResolver(assets);
            FileProviderRegistry.getInstance().addFileProvider(assetsFileResolver);
            ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
            String themeAssetsPath = "textmate/themes/" + name + ".json";
            InputStream is = FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath);
            IThemeSource source = IThemeSource.fromInputStream(is, themeAssetsPath, null);
            ThemeModel model = new ThemeModel(source, name);
            try {
                themeRegistry.loadTheme(model);
            } catch (Exception err) {
                err.printStackTrace();
                ErrorDialog.show(context, "Failed to load editor theme", err);
            }
        } catch (Exception err) {
            err.printStackTrace();
            ErrorDialog.show(context, "Failed to init editor theme", err);
        }
    }

    public static void changeEditorScheme(Context context, String theme) {
        try {
            currentTheme = theme;
            ThemeRegistry.getInstance().setTheme(currentTheme);
        } catch (Exception err) {
            err.printStackTrace();
            ErrorDialog.show(context, "Failed to change editor theme", err);
        }
    }
}
