package org.xedox.utils.sora;

import android.content.Context;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import java.util.HashMap;
import java.util.Map;

public class SoraTextMateLanguage extends TextMateLanguage {

    private final String scope;
    private final Context context;

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
}
