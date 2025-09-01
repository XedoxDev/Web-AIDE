package org.xedox.webaide.dialog;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xedox.utils.Assets;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.webaide.R;

public class WhatsNewDialog {

    private DialogBuilder builder;
    private ImageButton previous, next;
    private TextView text;
    private List<String> historyItems;
    private int currentIndex = 0;

    public WhatsNewDialog(Context context) {
        builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_whats_new);
        previous = builder.findViewById(R.id.previous);
        next = builder.findViewById(R.id.next);
        text = builder.findViewById(R.id.content);

        updateText(context);

        previous.setOnClickListener(v -> navigateToPrevious());
        next.setOnClickListener(v -> navigateToNext());

        builder.show();
    }

    private void updateText(Context context) {
        String locale = Locale.getDefault().getLanguage();
        String path = String.format("whats_new/%s.md", locale); // default: en.md

        historyItems = getHistory(context, path);
        if (historyItems.isEmpty()) {
            path = "whats_new/en.md";
            historyItems = getHistory(context, path);
        }

        updateNavigationState();
        displayCurrentItem();
    }

    private void navigateToPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            updateNavigationState();
            displayCurrentItem();
        }
    }

    private void navigateToNext() {
        if (currentIndex < historyItems.size() - 1) {
            currentIndex++;
            updateNavigationState();
            displayCurrentItem();
        }
    }

    private void updateNavigationState() {
        previous.setEnabled(currentIndex > 0);
        next.setEnabled(currentIndex < historyItems.size() - 1);
    }

    private void displayCurrentItem() {
        if (!historyItems.isEmpty() && currentIndex >= 0 && currentIndex < historyItems.size()) {
            String txt = historyItems.get(currentIndex);
            Markwon mw =
                    Markwon.builder(text.getContext())
                            .usePlugin(CorePlugin.create())
                            .usePlugin(ImagesPlugin.create())
                            .usePlugin(HtmlPlugin.create())
                            .usePlugin(TaskListPlugin.create(text.getContext()))
                            .usePlugin(TablePlugin.create(text.getContext()))
                            .build();
            mw.setMarkdown(text, txt);
        }
    }

    private List<String> getHistory(Context context, String asset) {
        List<String> history = new ArrayList<>();
        try {
            String full = Assets.from(context).readText(asset);
            Matcher m =
                    Pattern.compile("START:\\s*\\n(.*?)\\nEND", Pattern.DOTALL | Pattern.MULTILINE)
                            .matcher(full);
            while (m.find()) {
                String content = m.group(1).trim();
                history.add(content);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return history;
    }

    public static void show(Context context) {
        new WhatsNewDialog(context);
    }
}
