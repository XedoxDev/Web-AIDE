package org.xedox.webaide.editor;

import android.view.View;

public interface IEditor {
    
    void undo();
    void redo();
    void setCode(String newCode);
    String getCode();
    boolean canUndo();
    boolean canRedo();
    View getView();
}
