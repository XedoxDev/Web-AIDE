package org.xedox.webaide.editor;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xedox.webaide.util.io.Assets;

public class TML extends TextMateLanguage {
    private static final String SNIPPET_FILE_FORMAT = "textmate/%s/%s-snippets.json";

    private static final Map<String, String> SCOPE_MAPPING = new HashMap<>();

    static {
        SCOPE_MAPPING.put("source.html", "html");
        SCOPE_MAPPING.put("source.css", "css");
        SCOPE_MAPPING.put("source.js", "js");
        SCOPE_MAPPING.put("source.md", "markdown");
        SCOPE_MAPPING.put("source.py", "python");
        SCOPE_MAPPING.put("source.php", "php");
        SCOPE_MAPPING.put("source.js", "javascript");
    }

    private final String scope;
    private final Context context;

    public TML(String scopeName, Context context) {
        super(
                GrammarRegistry.getInstance().findGrammar(scopeName),
                GrammarRegistry.getInstance().findLanguageConfiguration(scopeName),
                GrammarRegistry.getInstance(),
                ThemeRegistry.getInstance(),
                true);
        this.scope = scopeName;
        this.context = context;
        String lang = SCOPE_MAPPING.get(scope);
        if (lang != null) {
            getSnippets(String.format(SNIPPET_FILE_FORMAT, lang, lang));
        }
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

        List<Snippet> snippets = getSnippets(String.format(SNIPPET_FILE_FORMAT, lang, lang));
        for (Snippet s : snippets) {
            if (s.getPrefix().startsWith(prefix) && !prefix.isEmpty()) {
                publisher.addItem(
                        new SimpleSnippetCompletionItem(
                                s.getPrefix(),
                                s.getDescription(),
                                new SnippetDescription(prefix.length(), s.getBuildedBody(), true)));
            }
        }
    }

    private List<Snippet> getSnippets(String path) {
        if (path == null) return Collections.emptyList();
        try {
            String json = Assets.from(context).asset(path).read();
            Snippet[] arr = new Gson().fromJson(json, Snippet[].class);
            for (Snippet s : arr) {
                s.setBuildedBody(CodeSnippetParser.parse(String.join("\n", s.getBody())));
            }
            List<Snippet> result = Arrays.asList(arr);
            return result;
        } catch (Exception e) {
            Log.e("TML", "Error loading snippets: " + path, e);
            return Collections.emptyList();
        }
    }

    private static class Snippet {
        @SerializedName("prefix")
        private String prefix;

        @SerializedName("description")
        private String description;

        @SerializedName("body")
        private String[] body;

        private CodeSnippet buildedBody;

        public String getPrefix() {
            return prefix;
        }

        public String getDescription() {
            return description;
        }

        public String[] getBody() {
            return body;
        }

        public CodeSnippet getBuildedBody() {
            return buildedBody;
        }

        public void setBuildedBody(CodeSnippet body) {
            this.buildedBody = body;
        }
    }
}
