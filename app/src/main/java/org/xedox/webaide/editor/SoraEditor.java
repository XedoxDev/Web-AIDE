package org.xedox.webaide.editor;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
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

    private void init() {
        setTypefaceText(
                Typeface.createFromAsset(getContext().getAssets(), "JetBrainsMono-Bold.ttf"));
        ViewGroup view = getComponent(EditorTextActionWindow.class).getView();
        view.setBackground(getContext().getDrawable(R.drawable.sora_editor_action_window_background));
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
}
