package org.xedox.webaide.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.rosemoe.sora.event.ContentChangeEvent;
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
    private String originalTitle;

    private FileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle extraArgs) {
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

        originalTitle = file.getName();
        changeTitle(originalTitle);

        editor.subscribeEvent(
                ContentChangeEvent.class,
                (s, event) -> {
                    if (!isModified) {
                        isModified = true;
                        changeTitle(originalTitle + "*");
                    }
                });

        return editor;
    }

    public static FileFragment newInstance(File file) {
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
        try {
            file.write(editor.getText().toString());
            isModified = false;
            changeTitle(originalTitle);
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }
    }

    public void saveAs(File newFile) {
        this.file = new FileX(newFile);
        this.originalTitle = file.getName();
        save();
    }
}
