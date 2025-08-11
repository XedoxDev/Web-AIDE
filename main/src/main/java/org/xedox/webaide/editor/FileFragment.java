package org.xedox.webaide.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import java.io.FileNotFoundException;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.FileX;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.editor.sora.SoraEditor;

public class FileFragment extends BaseFragment {

    private SoraEditor editor;
    private FileX file;

    private FileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle extraArgs) {
        editor = new SoraEditor(requireActivity());

        if (!file.exists()) {
            ErrorDialog.show(requireActivity(), new FileNotFoundException(file.getPath()));
            return editor;
        }
        try {
            editor.setText(file.read());
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }

        changeTitle(file.getName());

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
}
