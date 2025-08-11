package org.xedox.webaide.editor.sora;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.util.Set;
import java.util.HashSet;
import org.xedox.webaide.R;

public class SoraEditor extends CodeEditor {

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
        Typeface typeface =
                Typeface.createFromAsset(getContext().getAssets(), "JetBrainsMono-Regular.ttf");
        setTypefaceText(typeface);
        setTypefaceLineNumber(typeface);
    }

    public void append(CharSequence txt) {
        if (txt == null || txt.length() == 0) return;
        Content content = getText();
        content.insert(
                getText().getLineCount() - 1,
                getText().getColumnCount(getText().getLineCount() - 1),
                txt);
    }

    public String getStringText() {
        return getText().toString();
    }
    
    protected void scrollToEnd() {
        int lastLine = getText().getLineCount() - 1;
        ensurePositionVisible(lastLine, 0);
    }

    public void clear() {
        getText().replace(0, getText().length(), "");
    }

    public static class PrintStream extends java.io.PrintStream {
        private final SoraEditor editor;
        private final StringBuilder buffer = new StringBuilder();
        private static final int FLUSH_LIMIT = 20;

        public PrintStream(SoraEditor editor) {
            super(System.out);
            this.editor = editor;
        }

        @Override
        public void write(int b) {
            buffer.append((char) b);
            checkFlush();
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            buffer.append(new String(buf, off, len));
            checkFlush();
        }

        @Override
        public void println() {
            write('\n');
        }

        @Override
        public void println(String x) {
            print(x);
            println();
        }

        @Override
        public void print(String s) {
            if (s == null) {
                s = "null";
            }
            write(s.getBytes(), 0, s.length());
        }

        private void checkFlush() {
            if (buffer.length() >= FLUSH_LIMIT) {
                flush();
            }
        }

        @Override
        public void flush() {
            if (buffer.length() > 0) {
                final String text = buffer.toString();
                editor.post(() -> editor.append(text));
                buffer.setLength(0);
            }
        }

        @Override
        public void close() {
            flush();
        }
    }

}