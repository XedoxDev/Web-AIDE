package org.xedox.webaide.sora;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.xedox.utils.FileX;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.AppCore;
import static org.xedox.webaide.AppCore.*;

public class SoraEditorManager {
    private static Context context;
    public static final Handler handler = new Handler(Looper.getMainLooper());
    public static String font;
    public static boolean initialized = false;

    public static void initialize(Context context) {
        SoraEditorManager.context = context;

        FileProviderRegistry.getInstance().addFileProvider(new ResourceFileResolver());
        try {
            for (Theme theme : getThemesList()) {
                loadTheme(theme.fullPath, theme.name);
            }
        } catch (Exception err) {
            //ErrorDialog.show(context, err);
        }
        try {
            FileX langsJson = new FileX(dir("textmate"), "languages.json");
            if (!langsJson.exists()) {
                if (langsJson.createNewFile()) {
                    langsJson.write(generateLanguagesJson(dir("textmate") + "languages/"));
                }
            }
            GrammarRegistry.getInstance().loadGrammars(langsJson.getAbsolutePath());
        } catch (Exception err) {
            ErrorDialog.show(context, err);
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        font = sp.getString("editor_font", AppCore.dir("fonts") + "SourceCodePro-Regular.ttf");

        updateTheme(sp.getString("editor_theme", "Darcula"));
        initialized = true;
    }

    public static void updateFont(String path) {
        font = path;
    }

    public static void updateTheme(String name) {
        try {
            ThemeRegistry.getInstance().setTheme(name);
        } catch (Throwable e) {
            ErrorDialog.show(context, e);
        }
    }

    public static void loadTheme(String themePath, String name) {
        try (InputStream is = FileProviderRegistry.getInstance().tryGetInputStream(themePath)) {
            IThemeSource source = IThemeSource.fromInputStream(is, themePath, null);
            ThemeRegistry.getInstance().loadTheme(new ThemeModel(source, name));
        } catch (Exception err) {
            ErrorDialog.show(context, err);
        }
    }

    public static Language getLanguageByScopeName(String scopeName) throws IOException {
        Language[] langs = getLanguagesList(dir("textmate") + "languages/");
        for (Language lang : langs) {
            if (lang.scopeName.equals(scopeName)) return lang;
        }
        return null;
    }

    public static Language[] getLanguagesList(String languagesPath) throws IOException {
        List<Language> languages = new ArrayList<>();
        Gson gson = new Gson();

        File languagesDirectory = new File(languagesPath);
        File[] languageDirectories = languagesDirectory.listFiles();
        if (languageDirectories == null) {
            return languages.toArray(new Language[0]);
        }

        for (File languageDir : languageDirectories) {
            if (!languageDir.isDirectory()) continue;
            File syntaxesDir = new File(languageDir, "syntaxes");
            File[] grammarFiles =
                    syntaxesDir.listFiles(
                            (dir, name) ->
                                    name.endsWith(".tmLanguage.json")
                                            || name.endsWith(".tmLanguage"));

            if (grammarFiles == null || grammarFiles.length == 0) {
                continue;
            }

            File grammarFile = grammarFiles[0];

            try {
                String grammarContent = FileX.read(grammarFile);
                JsonObject jsonObject = gson.fromJson(grammarContent, JsonObject.class);

                Language language = new Language();
                language.name =
                        jsonObject.has("name")
                                ? jsonObject.get("name").getAsString()
                                : languageDir.getName();
                language.scopeName =
                        jsonObject.has("scopeName")
                                ? jsonObject.get("scopeName").getAsString()
                                : "";
                language.grammar = grammarFile.getAbsolutePath();
                language.fullPath = languageDir.getAbsolutePath();

                File langConfig = new File(languageDir, "language-configuration.json");
                if (langConfig.exists()) {
                    language.languageConfiguration = langConfig.getAbsolutePath();
                } else {
                    language.languageConfiguration = "";
                }

                File snippets = new File(languageDir, "snippets.json");
                if (!snippets.exists()) {
                    snippets =
                            new File(languageDir, language.name.toLowerCase() + ".snippets.json");
                }
                if (snippets.exists()) {
                    language.snippets = snippets.getAbsolutePath();
                }

                languages.add(language);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return languages.toArray(new Language[0]);
    }

    public static Theme getThemeByName(String name) throws IOException {
        for (Theme theme : getThemesList(dir("themes"))) {
            if (name.equals(theme.name)) return theme;
        }
        return null;
    }

    public static Theme[] getThemesList() throws IOException {
        return getThemesList(dir("themes"));
    }

    public static Theme[] getThemesList(String themesPath) throws IOException {
        List<Theme> themes = new ArrayList<>();
        Gson gson = new Gson();

        File themesDirectory = new File(themesPath);
        File[] themeFiles = themesDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (themeFiles == null) {
            return themes.toArray(new Theme[0]);
        }

        for (File themeFile : themeFiles) {
            if (themeFile.isDirectory()) continue;

            String themeContent = FileX.read(themeFile);
            JsonObject jsonObject = gson.fromJson(themeContent, JsonObject.class);
            Theme theme = new Theme();
            theme.fullPath = themeFile.getAbsolutePath();
            theme.name = jsonObject.get("name").getAsString();
            themes.add(theme);
        }
        return themes.toArray(new Theme[0]);
    }

    public static String generateLanguagesJson(String languagesPath) throws IOException {
        Language[] languages = getLanguagesList(languagesPath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        JsonArray langsArr = new JsonArray();
        for (Language lang : languages) {
            JsonObject jsonLang = new JsonObject();
            jsonLang.addProperty("name", lang.name);
            jsonLang.addProperty("scopeName", lang.scopeName);
            jsonLang.addProperty("grammar", lang.grammar);
            jsonLang.addProperty("languageConfiguration", lang.languageConfiguration);
            if (lang.snippets != null) {
                jsonLang.addProperty("snippets", lang.snippets);
            }
            langsArr.add(jsonLang);
        }
        root.add("languages", langsArr);
        return gson.toJson(root);
    }

    public static class Language {
        public String fullPath;
        public String grammar;
        public String name;
        public String scopeName;
        public String languageConfiguration;
        public String snippets;
    }

    public static class Theme {
        public String fullPath;
        public String name;
    }
}
