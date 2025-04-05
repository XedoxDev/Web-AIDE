package org.xedox.webaide.editor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SubscriptionReceipt;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import org.xedox.webaide.R;
import org.xedox.webaide.io.FileX;
import org.xedox.webaide.io.IFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditorFragment extends Fragment {
    private static final String ARG_FILE_PATH = "file_path";
    private static final Map<String, String> LANGUAGE_SCOPE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(".txt", "source.txt");
        map.put(".html", "source.html");
        map.put(".js", "source.js");
        map.put(".css", "source.css");
        map.put(".md", "source.md");
        LANGUAGE_SCOPE_MAP = Collections.unmodifiableMap(map);
    }

    private IFile file;
    private boolean isSaved = true;
    private String title;
    private TabLayout.Tab tab;
    private SubscriptionReceipt<ContentChangeEvent> contentChangeSubscriber;
    
    public IEditor editorView;
    
    public static EditorFragment newInstance(IFile file) {
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, file.getFullPath());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_FILE_PATH)) {
            file = new FileX(args.getString(ARG_FILE_PATH));
            updateTabState();
        } else {
            throw new IllegalArgumentException("File path must be provided");
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sora_editor_fragment, container, false);
        editorView = view.findViewById(R.id.editor);
        setupEditor();

        if (file != null) {
            String content = file.read();
            if (content != null) {
                editorView.setCode(content);
            }
        }

        return view;
    }

    private void setupEditor() {
        if (editorView instanceof SoraEditor) {
            SoraEditor soraEditor = (SoraEditor) editorView;

            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
            soraEditor.setLayoutParams(params);

            contentChangeSubscriber =
                    soraEditor.subscribeEvent(
                            ContentChangeEvent.class,
                            (event, unsubscribe) -> handleContentChange());

            configureEditorLanguage(soraEditor);

            try {
                soraEditor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
            } catch (Exception e) {
                Log.e("EditorFragment", "Error setting color scheme", e);
            }
        }
    }

    private void configureEditorLanguage(SoraEditor editor) {
        if (file == null) return;

        String extension = file.getExtension().toLowerCase();
        String scopeName = LANGUAGE_SCOPE_MAP.getOrDefault(extension, "source.txt");

        try {
            TextMateLanguage language = new TML(scopeName, getActivity());
            editor.setEditorLanguage(language);
        } catch (Throwable e) {
            Log.e("EditorFragment", "Error setting language: " + scopeName, e);
        }
    }

    private void handleContentChange() {
        if (isSaved) {
            isSaved = false;
            updateTabState();
        }
    }

    public void save() {
        if (file == null || editorView == null) return;

        String code = editorView.getCode();
        if (code != null) {
            file.write(code);
            isSaved = true;
            updateTabState();
        }
    }

    public void updateTabState() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;

        title = file.getName() + (isSaved ? "" : "*");
        getActivity()
                .runOnUiThread(
                        () -> {
                            if (tab != null && !getActivity().isDestroyed()) {
                                tab.setText(title);
                            }
                        });
    }

    @Override
    public void onDestroyView() {
        if (editorView instanceof SoraEditor) {
            contentChangeSubscriber.unsubscribe();
            ((SoraEditor) editorView).release();
        }
        editorView = null;
        super.onDestroyView();
    }

    public IFile getFile() {
        return file;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public String getTitle() {
        return title;
    }

    public void setTab(TabLayout.Tab tab) {
        this.tab = tab;
        updateTabState();
    }
}
