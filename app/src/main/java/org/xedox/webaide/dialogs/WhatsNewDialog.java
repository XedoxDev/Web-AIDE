package org.xedox.webaide.dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import io.noties.markwon.Markwon;
import org.xedox.webaide.R;
import org.xedox.webaide.util.io.Assets;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WhatsNewDialog {
    private static final String TAG = "WhatsNewDialog";
    private static final String DEFAULT_LANGUAGE = "EN";
    private static final String CONTENT_FILE = "whats_new.md";
    private static final Pattern CONTENT_PATTERN =
            Pattern.compile("^([A-Z]{2}):(.*?)END$", Pattern.MULTILINE | Pattern.DOTALL);

    private final Context context;
    private final Markwon markwon;
    private final List<String> contentItems = new ArrayList<>();
    private int currentIndex = 0;
    private boolean useFlexmark;

    public static void show(@NonNull Context context) {
        new WhatsNewDialog(context).showDialog();
    }

    private WhatsNewDialog(@NonNull Context context) {
        this.context = context;
        this.markwon = Markwon.create(context);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String markdownRenderer = prefs.getString("markdown_display_type", "Markwon");
        this.useFlexmark = "Flexmark".equals(markdownRenderer);
        
        loadContent();
    }

    private void showDialog() {
        final DialogBuilder builder =
                new DialogBuilder(context)
                        .setView(R.layout.dialog_whats_new)
                        .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        final TextView textView = builder.findViewById(R.id.update_text);
        final ImageButton upButton = builder.findViewById(R.id.update_up);
        final ImageButton downButton = builder.findViewById(R.id.update_down);

        upButton.setOnClickListener(v -> navigateContent(-1, textView, upButton, downButton));
        downButton.setOnClickListener(v -> navigateContent(1, textView, upButton, downButton));

        updateContent(textView, upButton, downButton);
        builder.show();
    }

    private void loadContent() {
        try {
            String fullText = Assets.from(context).asset(CONTENT_FILE).read();
            String langCode = Locale.getDefault().getLanguage().toUpperCase();

            Matcher matcher = CONTENT_PATTERN.matcher(fullText);
            while (matcher.find()) {
                if (matcher.group(1).equals(langCode)) {
                    contentItems.add(matcher.group(2).trim());
                }
            }

            if (contentItems.isEmpty() && !langCode.equals(DEFAULT_LANGUAGE)) {
                matcher.reset();
                while (matcher.find()) {
                    if (matcher.group(1).equals(DEFAULT_LANGUAGE)) {
                        contentItems.add(matcher.group(2).trim());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading " + CONTENT_FILE, e);
        }
    }

    private void navigateContent(
            int direction, TextView textView, ImageButton upButton, ImageButton downButton) {
        int newIndex = currentIndex + direction;
        if (newIndex < 0 || newIndex >= contentItems.size()) {
            return;
        }
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        int time = 200;

        ObjectAnimator out = ObjectAnimator.ofFloat(textView, View.ALPHA, 1f, 0f);
        ObjectAnimator slideOut =
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, 0, -textView.getWidth());

        ValueAnimator outAnimator = ValueAnimator.ofFloat(0f, 1f);
        outAnimator.setDuration(time);
        outAnimator.addUpdateListener(
                animation -> {
                    float progress = animation.getAnimatedFraction();
                    out.setCurrentFraction(progress);
                    slideOut.setCurrentFraction(progress);
                });

        outAnimator.addListener(
                new AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        currentIndex = newIndex;
                        updateContent(textView, upButton, downButton);

                        textView.setAlpha(0f);
                        textView.setTranslationX(textView.getWidth());

                        ObjectAnimator inAlpha =
                                ObjectAnimator.ofFloat(textView, View.ALPHA, 0f, 1f);
                        ObjectAnimator slideIn =
                                ObjectAnimator.ofFloat(
                                        textView, View.TRANSLATION_X, textView.getWidth(), 0);

                        ValueAnimator inAnimator = ValueAnimator.ofFloat(0f, 1f);
                        inAnimator.setDuration(time);
                        inAnimator.addUpdateListener(
                                anim -> {
                                    float progress = anim.getAnimatedFraction();
                                    inAlpha.setCurrentFraction(progress);
                                    slideIn.setCurrentFraction(progress);
                                });

                        inAnimator.addListener(
                                new AnimatorListener() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        upButton.setEnabled(currentIndex > 0);
                                        downButton.setEnabled(
                                                currentIndex < contentItems.size() - 1);
                                    }
                                });

                        inAnimator.start();
                        inAlpha.start();
                        slideIn.start();
                    }
                });

        outAnimator.start();
        out.start();
        slideOut.start();
    }

    private void updateContent(TextView textView, ImageButton upButton, ImageButton downButton) {
        if (contentItems.isEmpty()) {
            textView.setText("");
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            return;
        }

        if (currentIndex >= 0 && currentIndex < contentItems.size()) {
            if (useFlexmark) {
                textView.setText(flexmarkToSpannable(contentItems.get(currentIndex)));
            } else {
                markwon.setMarkdown(textView, contentItems.get(currentIndex));
            }
        }
    }

    private SpannableString flexmarkToSpannable(String markdown) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        ));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        Node document = parser.parse(markdown);
        String html = renderer.render(document);

        return new SpannableString(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    private interface AnimatorListener extends ValueAnimator.AnimatorListener {
        @Override
        public default void onAnimationCancel(Animator arg0) {
            onAnimationEnd(arg0);
        }

        @Override
        public default void onAnimationRepeat(Animator arg0) {}

        @Override
        public default void onAnimationStart(Animator arg0) {}
    }
}