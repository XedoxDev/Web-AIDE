package org.xedox.webaide.sora;

import android.content.Context;
import com.google.gson.Gson;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import android.os.Bundle;
import java.nio.charset.StandardCharsets;
import java.util.List;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import java.util.Collections;
import org.xedox.utils.Assets;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import java.util.Arrays;
import android.util.Log;
import java.util.HashMap;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.sora.snippet.Snippet;

public class SoraTextMateLanguage extends TextMateLanguage {

    private final String scope;
    private final Context context;

    private static final Map<String, String> SCOPE_MAPPING = new HashMap<>();

    static {
        SCOPE_MAPPING.put("source.html", "html");
        SCOPE_MAPPING.put("source.css", "css");
        SCOPE_MAPPING.put("source.js", "javascript");
    }

    public SoraTextMateLanguage(Context context, String scopeName) {
        super(
                GrammarRegistry.getInstance().findGrammar(scopeName),
                GrammarRegistry.getInstance().findLanguageConfiguration(scopeName),
                GrammarRegistry.getInstance(),
                ThemeRegistry.getInstance(),
                true);
        this.scope = scopeName;
        this.context = context;
    }

    @Override
    public void requireAutoComplete(
            ContentReference content,
            CharPosition pos,
            CompletionPublisher publisher,
            Bundle bundle) {
        String prefix =
                CompletionHelper.computePrefix(content, pos, MyCharacter::isJavaIdentifierPart);
        String lang = SCOPE_MAPPING.getOrDefault(scope, null);
        if (lang == null) return;
        try {
            String path = SoraEditorManager.getLanguageByScopeName(scope).snippets;
            if (path == null) return;
            List<Snippet> snippets = getSnippets(path);
            for (Snippet s : snippets) {
                if (s.getPrefix().startsWith(prefix) && !prefix.isEmpty()) {
                    publisher.addItem(s.getComplectionItem());
                }
            }
        } catch (Exception err) {
            ErrorDialog.show(context, err);
        }
    }

    private List<Snippet> getSnippets(String path) {
        if (path == null) return Collections.emptyList();
        try {
            String json = Assets.from(context).readText(path, StandardCharsets.UTF_8);
            Snippet[] arr = new Gson().fromJson(json, Snippet[].class);
            for (Snippet s : arr) {
                s.setBuildedBody(CodeSnippetParser.parse(String.join("\n", s.getBody())));
            }
            List<Snippet> result = Arrays.asList(arr);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
