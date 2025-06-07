package org.xedox.webaide.console;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.Content;
import org.xedox.webaide.editor.SoraEditor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.xedox.webaide.editor.TML;

public class ConsoleView extends SoraEditor {
    public static final String TYPE_ERROR = "E";
    public static final String TYPE_WARNING = "W";
    public static final String TYPE_DEBUG = "D";
    public static final String TYPE_INFO = "I";

    private static final int TAG_MAX_LENGTH = 11;
    private static final String DEFAULT_PRINT_PATTERN = "[%s] %s %s %s\n";
    private static final String DEFAULT_TAG = "Solar2Droid";

    private Content content;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private String printPattern = DEFAULT_PRINT_PATTERN;
    private String tag = DEFAULT_TAG;

    public ConsoleView(Context context) {
        this(context, null);
    }

    public ConsoleView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ConsoleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        content = getText();
        setEditorLanguage(new TML("source.log", getContext()));
        try {
            setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
        } catch (Exception err) {
            err.printStackTrace();
        }

        setTextSizePx(30);
        setContextClickable(true);
        setClickable(false);
    }

    public void printError(String text) {
        printColoredText(text, TYPE_ERROR);
    }

    public void printText(String text) {
        printColoredText(text, TYPE_INFO);
    }

    public void printWarn(String text) {
        printColoredText(text, TYPE_WARNING);
    }

    public void printDebug(String text) {
        printColoredText(text, TYPE_DEBUG);
    }

    public void println(String text) {
        printText(text);
    }

    public void printf(String format, Object... args) {
        printText(String.format(format, args));
    }

    public void printStackTrace(Throwable e) {
        printError(e.toString());
        for (StackTraceElement element : e.getStackTrace()) {
            printError("    at " + element.toString());
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            printError("Caused by: " + cause.toString());
            printStackTrace(cause);
        }
    }

    private void printColoredText(String text, String type) {
        if (text == null || type == null) return;

        String time = timeFormat.format(new Date());
        String formatted = String.format(printPattern, formatTag(tag), time, type, text);
        append(formatted);
    }

    private String formatTag(String tag) {
        if (tag == null) {
            return " ".repeat(TAG_MAX_LENGTH);
        }

        if (tag.length() > TAG_MAX_LENGTH) {
            return tag.substring(0, TAG_MAX_LENGTH - 3) + "...";
        }

        if (tag.length() < TAG_MAX_LENGTH) {
            return String.format("%-" + TAG_MAX_LENGTH + "s", tag);
        }

        return tag;
    }

    public void append(CharSequence txt) {
        if (txt == null || txt.length() == 0) return;

        content.insert(
                content.getLineCount() - 1,
                content.getColumnCount(content.getLineCount() - 1),
                txt);
        scrollToEnd();
    }

    private void scrollToEnd() {
        int lastLine = content.getLineCount() - 1;
        ensurePositionVisible(lastLine, 0);
    }

    public void clear() {
        content.replace(0, content.length(), "");
    }
}