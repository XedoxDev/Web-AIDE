package org.xedox.utils.sora;

import android.content.Context;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.provider.FileResolver;
import java.io.InputStream;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.xedox.utils.dialog.ErrorDialog;

public class SoraEditorManager {
    public static void initialize(Context context) {
        try {
            FileResolver resolver =
                    new AssetsFileResolver(context.getApplicationContext().getAssets());
            FileProviderRegistry.getInstance().addFileProvider(resolver);
            loadTheme("darcula");
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json");
        } catch (Exception err) {
            err.printStackTrace();
            ErrorDialog.show(context, err);
        }
    }

    public static void loadTheme(String name) {
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
        String themeAssetsPath = "textmate/themes/" + name + ".json";
        InputStream is = FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath);
        IThemeSource source = IThemeSource.fromInputStream(is, themeAssetsPath, null);
        ThemeModel model = new ThemeModel(source, name);
        try {
            themeRegistry.loadTheme(model);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
