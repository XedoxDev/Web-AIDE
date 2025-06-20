package org.xedox.webaide.editor.format;

import android.content.SharedPreferences;
import org.xedox.webaide.IDE;

public interface IFormatter {
    public static final int[] tabSize = new int[1];

    public default String format(CharSequence source) {
        return format(source, tabSize[0]);
    }

    String format(CharSequence source, int tabsize);

    public static void setTabSize(int newTabSize) {
        tabSize[0] = newTabSize;
    }
    
    public static void updateTabSize(SharedPreferences prefs) {
    	setTabSize(prefs.getInt("tab_size", 4));
    }

    public static int getTabSize() {
        return tabSize[0];
    }
}
