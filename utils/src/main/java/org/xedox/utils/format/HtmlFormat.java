package org.xedox.utils.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

public class HtmlFormat implements IFormat {
    public String format(CharSequence source) {
        try {
            String text = source.toString();
            Document doc = Jsoup.parse(text);
            Document.OutputSettings settings = doc.outputSettings();
            settings.indentAmount(FormatConfig.getInstance().getUseTab() ? 4 : FormatConfig.getInstance().getIndentSize());
            settings.prettyPrint(true);
            settings.charset("UTF-8");
            settings.escapeMode(Entities.EscapeMode.xhtml);
            settings.syntax(Document.OutputSettings.Syntax.html);
            if (!FormatConfig.getInstance().getUseTab()) return doc.toString();
            else doc.toString().replaceAll("    ", "\t");
        } catch (Exception err) {
            err.printStackTrace();
        }
        return source.toString();
    }
}
