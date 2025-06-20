package org.xedox.webaide.util;

import android.content.Context;
import android.text.SpannableString;
import androidx.annotation.NonNull;

import androidx.preference.PreferenceManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;

import java.util.Arrays;

import io.noties.markwon.Markwon;

public class MarkdownManager {
    private final Markwon markwon;
    private final Parser flexmarkParser;
    private final HtmlRenderer flexmarkRenderer;
    private MarkdownType markdownType;

    public MarkdownManager(@NonNull Context context, @NonNull MarkdownType markdownType) {
        this.markwon = Markwon.create(context);
        this.markdownType = markdownType;
        
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        ));
        this.flexmarkParser = Parser.builder(options).build();
        this.flexmarkRenderer = HtmlRenderer.builder(options).build();
    }
    
    public MarkdownManager(@NonNull Context context) {
        this(context, null);
        setMarkdownType( getTypePreference(context));
    }
    
    private MarkdownType getTypePreference(Context context) {
        String stringtype = PreferenceManager.getDefaultSharedPreferences(context).getString("markdown_display_type", "markwon");
        MarkdownType type = MarkdownType.MARKWON;
        switch(stringtype) {
            case "Flexmark": type = MarkdownType.FLEXMARK; break;
            case "Markwon": type = MarkdownType.MARKWON; break;
        }
        return type;
    }


    public void setMarkdownType(@NonNull MarkdownType markdownType) {
        this.markdownType = markdownType;
    }

    public MarkdownType getMarkdownType() {
        return markdownType;
    }

    public SpannableString toSpannable(@NonNull String markdown) {
        switch (markdownType) {
            case FLEXMARK:
                return flexmarkToSpannable(markdown);
            case MARKWON:
            default:
                return markwonToSpannable(markdown);
        }
    }

    private SpannableString markwonToSpannable(@NonNull String markdown) {
        return new SpannableString(markwon.toMarkdown(markdown));
    }

    private SpannableString flexmarkToSpannable(@NonNull String markdown) {
        Node document = flexmarkParser.parse(markdown);
        String html = flexmarkRenderer.render(document);
        return new SpannableString(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
    }
}