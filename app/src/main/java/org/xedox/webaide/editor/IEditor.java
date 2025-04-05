package org.xedox.webaide.editor;

import android.view.View;

public interface IEditor {
    
    public static final int TXT = 0;
    public static final int HTML = 1;
    
    void undo();
    void redo();
    void setCode(String newCode);
    String getCode();
    boolean canUndo();
    boolean canRedo();
    View getView();
}
