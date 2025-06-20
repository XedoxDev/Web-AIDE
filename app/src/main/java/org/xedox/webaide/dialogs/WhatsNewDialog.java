package org.xedox.webaide.dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import org.xedox.webaide.R;
import org.xedox.webaide.util.io.Assets;
import org.xedox.webaide.util.MarkdownManager;
import org.xedox.webaide.util.MarkdownType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WhatsNewDialog {
    private static final String TAG = "WhatsNewDialog";
    private static final String CONTENT_DIR = "whats_new/";
    private static final String DEFAULT_LANGUAGE = "EN";
    private static final Pattern UPDATE_PATTERN = Pattern.compile("START:(.*?)END", Pattern.DOTALL);

    private final Context context;
    private final MarkdownManager markdownManager;
    private final List<String> updates = new ArrayList<>();
    private int currentUpdateIndex = 0;

    private TextView updateTextView;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private DialogBuilder dialogBuilder;

    public static void show(@NonNull Context context) {
        new WhatsNewDialog(context).showDialog();
    }

    private WhatsNewDialog(@NonNull Context context) {
        this.context = context;
        this.markdownManager = new MarkdownManager(context);
        loadUpdates();
    }

    private String getLanguageSuffix() {
        // Получаем текущий язык устройства (например, "ru", "en" и т.д.)
        String language = Locale.getDefault().getLanguage().toUpperCase();
        
        // Проверяем, существует ли файл для этого языка
        String fileName = CONTENT_DIR + language + ".md";
        try {
            if (Assets.from(context).assetExists(fileName)) {
                return language;
            }
        } catch (Exception e) {
            Log.d(TAG, "File not found for language: " + language);
        }
        
        // Возвращаем язык по умолчанию, если файл не найден
        return DEFAULT_LANGUAGE;
    }

    private void loadUpdates() {
        String language = getLanguageSuffix();
        String contentFile = CONTENT_DIR + language + ".md";
        
        try {
            String content = Assets.from(context).asset(contentFile).read();
            Matcher matcher = UPDATE_PATTERN.matcher(content);
            while (matcher.find()) {
                String update = matcher.group(1).trim();
                if (!update.isEmpty()) {
                    updates.add(update);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading updates from file: " + contentFile, e);
            
            // Попробуем загрузить английскую версию как fallback
            if (!language.equals(DEFAULT_LANGUAGE)) {
                try {
                    contentFile = CONTENT_DIR + DEFAULT_LANGUAGE + ".md";
                    String content = Assets.from(context).asset(contentFile).read();
                    Matcher matcher = UPDATE_PATTERN.matcher(content);
                    while (matcher.find()) {
                        String update = matcher.group(1).trim();
                        if (!update.isEmpty()) {
                            updates.add(update);
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error loading default language updates", ex);
                }
            }
        }
    }

    private void showDialog() {
        dialogBuilder =
                new DialogBuilder(context)
                        .setView(R.layout.dialog_whats_new)
                        .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        updateTextView = dialogBuilder.findViewById(R.id.update_text);
        prevButton = dialogBuilder.findViewById(R.id.update_up);
        nextButton = dialogBuilder.findViewById(R.id.update_down);

        prevButton.setOnClickListener(v -> showUpdate(-1));
        nextButton.setOnClickListener(v -> showUpdate(1));

        showUpdate(0);
        dialogBuilder.show();
    }

    public void updateContent(String markdown) {
        updateTextView.setText(markdownManager.toSpannable(markdown));
        // Обновляем состояние кнопок навигации
        prevButton.setEnabled(currentUpdateIndex > 0);
        nextButton.setEnabled(currentUpdateIndex < updates.size() - 1);
    }

    public void showUpdate(int delta) {
        int newIndex = currentUpdateIndex + delta;

        if (newIndex < 0 || newIndex >= updates.size()) {
            return;
        }

        currentUpdateIndex = newIndex;
        updateContent(updates.get(currentUpdateIndex));
    }
}