package org.xedox.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.widget.CodeEditor;

public class TextFragment extends Fragment {
    
    private static final String ARG_SCOPE_NAME = "scope_name";
    private CodeEditor editor;

    public static TextFragment newInstance(String scopeName) {
        TextFragment f = new TextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCOPE_NAME, scopeName);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String scopeName = requireArguments().getString(ARG_SCOPE_NAME);
        if (scopeName == null) {
            throw new IllegalStateException("Scope name must not be null");
        }

        editor = new CodeEditor(requireActivity());
        try {
            editor.setEditorLanguage(TextMateLanguage.create(scopeName, GrammarRegistry.getInstance(), false));
            editor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
        } catch (Exception err) {
            err.printStackTrace();
        }
        editor.setSoftKeyboardEnabled(false);
        return editor;
    }

    public CodeEditor getEditor() {
        return this.editor;
    }
}
