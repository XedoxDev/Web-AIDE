package org.xedox.webaide.util;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class HighlightText {
    public static void highlight(TextView textView, String textToHighlight, int colorResId) {
        String orig = textView.getText().toString();
        SpannableString spannableString = new SpannableString(orig);
        int lastIndex = 0;

        while (lastIndex != -1) {
            lastIndex = orig.indexOf(textToHighlight, lastIndex);

            if (lastIndex != -1) {
                spannableString.setSpan(
                        new ForegroundColorSpan(
                                ContextCompat.getColor(textView.getContext(), colorResId)),
                        lastIndex,
                        lastIndex + textToHighlight.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lastIndex += textToHighlight.length();
            }
        }

        textView.setText(spannableString);
    }

    public static void clickable(
            TextView textView, String textToHighlight, int colorResId, OnClickListener ocl) {
        SpannableString spannableString = new SpannableString(textView.getText());
        String orig = textView.getText().toString();
        int lastIndex = 0;

        while (lastIndex != -1) {
            lastIndex = orig.indexOf(textToHighlight, lastIndex);

            if (lastIndex != -1) {
                int start = lastIndex;
                int end = lastIndex + textToHighlight.length();

                ClickableSpan clickableSpan =
                        new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                ocl.onClick();
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(
                                        ContextCompat.getColor(
                                                textView.getContext(), colorResId)); // Цвет ссылки
                                ds.setUnderlineText(true);
                            }
                        };

                spannableString.setSpan(
                        clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lastIndex += textToHighlight.length();
            }
        }

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    public interface OnClickListener {
        void onClick();
    }
}
