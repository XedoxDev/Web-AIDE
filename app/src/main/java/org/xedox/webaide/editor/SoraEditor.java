package org.xedox.webaide.editor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.PreferenceManager;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.util.Set;
import java.util.HashSet;
import org.xedox.webaide.IDE;
import org.xedox.webaide.R;

public class SoraEditor extends CodeEditor implements IEditor {

    public SoraEditor(Context context) {
        super(context);
        init();
    }

    public SoraEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoraEditor(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
        init();
    }

    protected void init() {
        SharedPreferences m = PreferenceManager.getDefaultSharedPreferences(getContext());
        String fontS = m.getString("editor_typeface", "jetbrainsmono-bold");

        Typeface font;
        switch (fontS) {
            case "sans_serif":
                font = Typeface.SANS_SERIF;
                break;
            case "serif":
                font = Typeface.SERIF;
                break;
            case "monospace":
                font = Typeface.MONOSPACE;
                break;
            case "custom":
                font = Typeface.createFromFile(IDE.HOME + "font.ttf");
                break;
            case "jetbrainsmono-bold":
            default:
                font = Typeface.createFromAsset(getContext().getAssets(), "JetBrainsMono-Bold.ttf");
                break;
        }

        setTypefaceText(font);

        Set<String> flags = m.getStringSet("non_printable_flags", new HashSet<>());

        int paintingFlags = 0;

        for (String flag : flags) {
            switch (flag) {
                case "leading":
                    paintingFlags |= CodeEditor.FLAG_DRAW_WHITESPACE_LEADING;
                    break;
                case "line_separator":
                    paintingFlags |= CodeEditor.FLAG_DRAW_LINE_SEPARATOR;
                    break;
                case "in_selection":
                    paintingFlags |= CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION;
                    break;
                case "trailing":
                    paintingFlags |= CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING;
                    break;
                case "empty_line":
                    paintingFlags |= CodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE;
                    break;
                case "tab":
                    paintingFlags |= CodeEditor.FLAG_DRAW_TAB_SAME_AS_SPACE;
                    break;
            }
        }
        setLineNumberEnabled(m.getBoolean("line_numbers", true));

        setNonPrintablePaintingFlags(paintingFlags);

        EditorTextActionWindow actionWindow = getComponent(EditorTextActionWindow.class);
        if (actionWindow != null) {
            ViewGroup view = actionWindow.getView();
            if (view != null) {
                view.setBackground(
                        getContext().getDrawable(R.drawable.sora_editor_action_window_background));
            }
        }
        setBlockLineEnabled(m.getBoolean("block_line", false));
    }

    @Override
    public void setCode(String code) {
        setText(code);
    }

    @Override
    public String getCode() {
        return getText().toString();
    }

    @Override
    public View getView() {
        return this;
    }

    public void moveLeft() {
        CharPosition cp = getSelectingTarget();
        cp.index--;
    }

    public void moveRight() {
        CharPosition cp = getSelectingTarget();
        cp.index++;
    }
}
