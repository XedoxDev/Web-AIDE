package org.xedox.webaide.dialogs;

import android.content.Context;
import android.text.Spanned;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import io.noties.markwon.Markwon;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.util.io.Assets;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class WhatsNewDialog {

    private static final String TAG = "WhatsNewDialog";
    private static final String DEFAULT_LANGUAGE = "EN";
    private static final String CONTENT_FILE = "whats_new.md";
    private static final Pattern CONTENT_PATTERN = 
        Pattern.compile("^([A-Z]{2}):(.*?)END$", Pattern.MULTILINE | Pattern.DOTALL);

    private Context context;
    private List<String> contentItems = new ArrayList<>();
    private int currentIndex = 0;
    private Markwon markwon;
    private TextView textView;
    private ImageButton upButton;
    private ImageButton downButton;

    public WhatsNewDialog(Context context) {
        this.context = context;
        this.markwon = Markwon.create(context);
    }
    
    public static void show(Context context) {
    	new WhatsNewDialog(context).show();
    }

    public void show() {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setView(R.layout.whats_new_dialog);
        
        loadContentItems();
        
        textView = builder.findViewById(R.id.update_text);
        upButton = builder.findViewById(R.id.update_up);
        downButton = builder.findViewById(R.id.update_down);
        
        updateContent();
        
        upButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateContent();
            }
        });
        
        downButton.setOnClickListener(v -> {
            if (currentIndex < contentItems.size() - 1) {
                currentIndex++;
                updateContent();
            }
        });
        
        updateNavigationButtons();
        
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    private void loadContentItems() {
        String langCode = Locale.getDefault().getLanguage().toUpperCase();
        String fullText = loadWhatsNewFile();
        
        if (!fullText.isEmpty()) {
            // Добавляем контент для текущего языка
            List<String> items = extractAllContentForLanguage(fullText, langCode);
            contentItems.addAll(items);
            
            // Если нет контента для текущего языка, добавляем контент по умолчанию
            if (contentItems.isEmpty() && !langCode.equals(DEFAULT_LANGUAGE)) {
                items = extractAllContentForLanguage(fullText, DEFAULT_LANGUAGE);
                contentItems.addAll(items);
            }
        }
    }
    
    private String loadWhatsNewFile() {
        try {
            return Assets.from(context).asset(CONTENT_FILE).read();
        } catch(Exception err) {
            Log.e(TAG, "Error reading " + CONTENT_FILE, err);
            return "";
        }
    }
    
    private List<String> extractAllContentForLanguage(String fullText, String languageCode) {
        List<String> items = new ArrayList<>();
        Matcher matcher = CONTENT_PATTERN.matcher(fullText);
        
        while (matcher.find()) {
            if (matcher.group(1).equals(languageCode)) {
                items.add(matcher.group(2).trim());
            }
        }
        
        return items;
    }
    
    private void updateContent() {
        if (!contentItems.isEmpty() && currentIndex >= 0 && currentIndex < contentItems.size()) {
            final Spanned markdown = markwon.toMarkdown(contentItems.get(currentIndex));
            textView.setText(markdown);
            updateNavigationButtons();
        }
    }
    
    private void updateNavigationButtons() {
        upButton.setEnabled(currentIndex > 0);
        downButton.setEnabled(currentIndex < contentItems.size() - 1);
    }
}