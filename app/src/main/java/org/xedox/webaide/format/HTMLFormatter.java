package org.xedox.webaide.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xedox.webaide.IDE;

public class HTMLFormatter implements IFormatter {

    @Override
    public String format(CharSequence source, int tabsize) {
        Document doc = Jsoup.parse((String)source);
        doc.outputSettings().indentAmount(tabsize);
        return doc.html();
    }
}
