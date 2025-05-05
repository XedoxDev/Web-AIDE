package org.xedox.webaide.dialogs;

import android.content.Context;
import android.text.Spanned;
import android.util.Log;

import android.widget.TextView;
import io.noties.markwon.Markwon;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.io.Assets;

import static org.xedox.webaide.dialogs.DialogBuilder.*;

public class WhatsNewDialog {

    private static final String TAG = "WhatsNewDialog";
    private static final String DEFAULT_LANGUAGE = "EN";
    private static final String CONTENT_FILE = "whats_new.txt";
    private static final Pattern CONTENT_PATTERN = 
        Pattern.compile("^([A-Z]{2}):(.*?)END$", Pattern.MULTILINE | Pattern.DOTALL);

    public static void show(Context context) {
        DialogBuilder builder = new DialogBuilder(context);
        String content = getLocalizedWhatsNewText(context);
        
        TextView text = new TextView(context);
        int padd = 40;
        text.setPadding(padd, padd, padd, padd);
        builder.setView(text);
        
        Markwon markwon = Markwon.create(context);
        final Spanned markdown = markwon.toMarkdown(content);
        text.setText(markdown);
        
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> EXIT);

        builder.show();
    }
    
    private static String getLocalizedWhatsNewText(Context context) {
        String langCode = Locale.getDefault().getLanguage().toUpperCase();
        String fullText = loadWhatsNewFile(context);
        
        if (fullText.isEmpty()) {
            return "";
        }

        String content = extractContentForLanguage(fullText, langCode);
        if (!content.isEmpty()) {
            return content;
        }

        if (!langCode.equals(DEFAULT_LANGUAGE)) {
            content = extractContentForLanguage(fullText, DEFAULT_LANGUAGE);
        }

        return content;
    }

    private static String loadWhatsNewFile(Context context) {
        try {
            return Assets.from(context).asset(CONTENT_FILE).read();
        } catch(Exception err) {
            Log.e(TAG, "Error reading " + CONTENT_FILE, err);
            return "";
        }
    }

    private static String extractContentForLanguage(String fullText, String languageCode) {
        Matcher matcher = CONTENT_PATTERN.matcher(fullText);
        
        while (matcher.find()) {
            if (matcher.group(1).equals(languageCode)) {
                return matcher.group(2).trim();
            }
        }
        
        return "";
    }
}