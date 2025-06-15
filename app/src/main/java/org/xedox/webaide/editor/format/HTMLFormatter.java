package org.xedox.webaide.editor.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLFormatter implements IFormatter {

    @Override
    public String format(CharSequence source, int tabsize) {
        Document doc = Jsoup.parse((String)source);
        doc.outputSettings().indentAmount(tabsize);
        return doc.html();
    }
}
