package org.xedox.webaide.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SubscriptionReceipt;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import java.io.File;
import java.io.FileNotFoundException;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.FileX;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.editor.sora.SoraEditor;
import org.xedox.webaide.editor.sora.SoraTextMateLanguage;

public class FileFragment extends BaseFragment {

    private SoraEditor editor;
    private FileX file;
    private boolean isModified = false;
    private String title;
    
    private SubscriptionReceipt contentChangeEvent;

    private FileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle extraArgs) {
        if (file == null) {
            throw new IllegalStateException("File must be set via newInstance()");
        }

        editor = new SoraEditor(requireActivity());
        try {
            editor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
        } catch (Exception err) {
            err.printStackTrace();
        }
        editor.setEditorLanguage(
                new SoraTextMateLanguage(requireActivity(), "source" + file.getExtension()));
        
        if (!file.exists()) {
            ErrorDialog.show(requireActivity(), new FileNotFoundException(file.getPath()));
            return editor;
        }

        try {
            editor.setText(file.read());
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }

        contentChangeEvent = editor.subscribeEvent(
                ContentChangeEvent.class,
                (s, event) -> {
                    if (!isModified) {
                        isModified = true;
                        updateTitle();
                    }
                });

        updateTitle();
        return editor;
    }

    @Override
    public void onDestroyView() {
        if (editor != null && contentChangeEvent != null) {
            contentChangeEvent.unsubscribe();
        }
        super.onDestroyView();
    }
    
    @Override
    public String getTitle() {
        return title;
    }

    private void updateTitle() {
        title = file.getName() + (isModified ? "*" : "");
        changeTitle(title);
    }

    public static FileFragment newInstance(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        FileFragment ff = new FileFragment();
        ff.file = new FileX(file);
        return ff;
    }

    public SoraEditor getEditor() {
        return editor;
    }

    public FileX getFile() {
        return file;
    }

    public boolean isModified() {
        return isModified;
    }

    public void save() {
        if (editor == null || editor.getText() == null) {
            return;
        }
        try {
            file.write(editor.getText().toString());
            isModified = false;
            updateTitle();
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }
    }

    @Override
    public void changeTitle(String title) {
        this.title = title;
        super.changeTitle(title);
    }

    public void saveAs(File newFile) {
        if (newFile == null) {
            throw new IllegalArgumentException("New file cannot be null");
        }
        this.file = new FileX(newFile);
        save();
    }
}