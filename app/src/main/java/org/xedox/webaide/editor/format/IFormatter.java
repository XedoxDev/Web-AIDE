package org.xedox.webaide.editor.format;

import org.xedox.webaide.IDE;

public interface IFormatter {
    public default String format(CharSequence source) {
        return format(source, IDE.TAB_SIZE);
    }
    
    String format(CharSequence source, int tabsize);
}
