package org.xedox.webaide.sora.snippet;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;

public class SnippetCompletionItem extends CompletionItem {

    private final SnippetDescription snippet;

    public SnippetCompletionItem(CharSequence label, SnippetDescription snippet) {
        this(label, null, snippet);
    }

    public SnippetCompletionItem(
            CharSequence label, CharSequence desc, SnippetDescription snippet) {
        this(label, desc, null, snippet);
    }

    public SnippetCompletionItem(
            CharSequence label, CharSequence desc, Drawable icon, SnippetDescription snippet) {
        super(label, desc, icon);
        this.snippet = snippet;
        kind(CompletionItemKind.Snippet);
    }

    @Override
    public void performCompletion(
            @NonNull CodeEditor editor, @NonNull Content text, @NonNull CharPosition position) {
        int prefixLength = snippet.getSelectedLength();
        var selectedText =
                text.subSequence(position.index - prefixLength, position.index).toString();
        int actionIndex = position.index;
        if (snippet.getDeleteSelected()) {
            text.delete(position.index - prefixLength, position.index);
            actionIndex -= prefixLength;
        }
        editor.getSnippetController().startSnippet(actionIndex, snippet.getSnippet(), selectedText);
    }

    @Override
    public void performCompletion(
            @NonNull CodeEditor editor, @NonNull Content text, int line, int column) {}
}
