package org.xedox.webaide.editor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import org.xedox.utils.format.HtmlFormat;
import org.xedox.utils.format.IFormat;
import org.xedox.webaide.sora.SoraEditor;
import org.xedox.webaide.sora.SoraTextMateLanguage;

public class FileFragment extends BaseFragment {

    private SoraEditor editor;
    private FileX file;
    private boolean isModified = false;

    private SubscriptionReceipt contentChangeEvent;
    private IFormat format;

    private static final String KEY_FILE_PATH = "file_path";
    private static final String KEY_IS_MODIFIED = "is_modified";
    private static final String KEY_EDITOR_TEXT = "editor_text";
    private static final String KEY_CURSOR_POSITION = "cursor_position";

    private Handler handler = new Handler(Looper.getMainLooper());

    private FileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String filePath = savedInstanceState.getString(KEY_FILE_PATH);
            if (filePath != null) {
                file = new FileX(new File(filePath));
            }
            isModified = savedInstanceState.getBoolean(KEY_IS_MODIFIED, false);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        contentChangeEvent =
                editor.subscribeEvent(
                        ContentChangeEvent.class,
                        (s, event) -> {
                            if (!isModified) {
                                isModified = true;
                                changeTitle();
                            }
                        });
        return editor;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!file.exists()) {
            ErrorDialog.show(requireActivity(), new FileNotFoundException(file.getPath()));
            return;
        }

        try {
            if (savedInstanceState != null && savedInstanceState.containsKey(KEY_EDITOR_TEXT)) {
                String savedText = savedInstanceState.getString(KEY_EDITOR_TEXT);
                editor.setText(savedText);

                if (savedInstanceState.containsKey(KEY_CURSOR_POSITION)) {
                    int cursorPosition = savedInstanceState.getInt(KEY_CURSOR_POSITION, 0);
                    editor.getCursor().setLeft(cursorPosition, cursorPosition);
                }
            } else {
                editor.setText(file.read());
            }
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }

        changeTitle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (file != null) {
            outState.putString(KEY_FILE_PATH, file.getAbsolutePath());
        }

        outState.putBoolean(KEY_IS_MODIFIED, isModified);

        if (editor != null && editor.getText() != null) {
            outState.putString(KEY_EDITOR_TEXT, editor.getText().toString());
            outState.putInt(KEY_CURSOR_POSITION, editor.getCursor().getLeft());
        }
    }

    @Override
    public void onDestroyView() {
        if(editor != null) editor.release();
        if (editor != null && contentChangeEvent != null) {
            contentChangeEvent.unsubscribe();
        }
        super.onDestroyView();
    }

    @Override
    public String getTitle() {
        return file != null ? file.getName() + (isModified ? "*" : "") : "Untitled";
    }

    public static FileFragment newInstance(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        FileFragment ff = new FileFragment();
        ff.file = new FileX(file);
        return ff;
    }

    public static FileFragment newInstance(Bundle savedInstanceState) {
        FileFragment ff = new FileFragment();
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FILE_PATH)) {
            String filePath = savedInstanceState.getString(KEY_FILE_PATH);
            ff.file = new FileX(new File(filePath));
            ff.isModified = savedInstanceState.getBoolean(KEY_IS_MODIFIED, false);
        }
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

    public void setFormat(IFormat formatter) {
        this.format = formatter;
    }

    public void setFormat(Class<? extends IFormat> formatterClazz) {
        try {
            this.format = formatterClazz.getDeclaredConstructor().newInstance();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public boolean canFormat() {
        return format == null;
    }

    public void format() {
        if (canFormat()) return;
        new Thread(
                        () -> {
                            String formatted = format.format(editor.getText());
                            handler.post(() -> editor.setText(formatted));
                        })
                .start();
    }

    public void save() {
        if (editor == null || editor.getText() == null || file == null) {
            return;
        }
        try {
            file.write(editor.getText().toString());
            isModified = false;
            changeTitle();
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), err);
        }
    }

    public void saveAs(File newFile) {
        if (newFile == null) {
            throw new IllegalArgumentException("New file cannot be null");
        }
        this.file = new FileX(newFile);
        if (file.getName().endsWith(".html")) {
            format = new HtmlFormat();
        }
        save();
    }

    public void resetModifiedFlag() {
        isModified = false;
        changeTitle();
    }
}
