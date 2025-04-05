package org.xedox.webaide.console;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import androidx.appcompat.widget.AppCompatTextView;
import org.xedox.webaide.R;

public class ConsoleView extends AppCompatTextView {

    public ConsoleView(@NonNull Context context) {
        super(context, null);
        init();
    }

    public ConsoleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public ConsoleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextColor(getColor(R.color.tint));
        setTextIsSelectable(true);
        setClickable(true);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "JetBrainsMono-Bold.ttf"));
        setText("WebAIDE Console\n");
    }

    public void printError(String text) {
        printColoredText(text, getColor(R.color.console_error));
    }

    public void printText(String text) {
        printColoredText(text, getColor(R.color.tint));
    }

    public void printWarn(String text) {
        printColoredText(text, getColor(R.color.console_warn));
    }

    private void printColoredText(String text, int color) {
        SpannableString spannableString = new SpannableString(text + '\n');
        spannableString.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        append(spannableString);
    }

    @ColorInt
    private int getColor(int colorId) {
        return getContext().getColor(colorId);
    }
}