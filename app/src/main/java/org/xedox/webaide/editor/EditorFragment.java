package org.xedox.webaide.editor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.xedox.webaide.R;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.format.*;
import org.xedox.webaide.io.FileX;
import org.xedox.webaide.io.IFile;

public class EditorFragment extends Fragment {
    private static final String ARG_FILE_PATH = "file_path";
    private static final Map<String, String> LANGUAGE_SCOPE_MAP = createLanguageScopeMap();

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private IFile file;
    private boolean isSaved = true;
    private String title;
    private WeakReference<TabLayout.Tab> tabRef;
    private SubscriptionReceipt<ContentChangeEvent> contentChangeSubscriber;
    public SoraEditor editorView;
    private String extension;
    private IFormatter formatter;

    private static Map<String, String> createLanguageScopeMap() {
        Map<String, String> map = new HashMap<>();
        map.put(".txt", "source.txt");
        map.put(".html", "source.html");
        map.put(".js", "source.js");
        map.put(".css", "source.css");
        map.put(".md", "source.md");
        map.put(".lua", "source.lua");
        map.put(".php", "source.php");
        map.put(".py", "source.py");
        return Collections.unmodifiableMap(map);
    }

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
        if (args == null || !args.containsKey(ARG_FILE_PATH)) {
            throw new IllegalArgumentException("File path must be provided");
        }
        file = new FileX(args.getString(ARG_FILE_PATH));
        updateTabState();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sora_editor_fragment, container, false);
        editorView = view.findViewById(R.id.editor);

        backgroundExecutor.execute(() -> {
            try {
                setupEditor();
                loadFileContent();
            } catch (Exception e) {
                Log.e("EditorFragment", "Background setup failed", e);
            }
        });

        return view;
    }

    private void setupEditor() {
        if (editorView == null) return;

        mainHandler.post(() -> {
            editorView.setLayoutParams(createLayoutParams());
            contentChangeSubscriber = editorView.subscribeEvent(
                    ContentChangeEvent.class, (event, unsubscribe) -> handleContentChange());
            configureEditorLanguage(editorView);
            setupColorScheme(editorView);
        });
    }

    private RelativeLayout.LayoutParams createLayoutParams() {
        return new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    private void loadFileContent() {
        if (file == null) return;

        String content = file.read();
        if (content != null) {
            mainHandler.post(() -> {
                if (editorView != null) {
                    setCode(content);
                }
            });
        }
    }

    private void configureEditorLanguage(SoraEditor editor) {
        if (file == null) return;

        String extension = file.getExtension().toLowerCase();
        String scopeName = LANGUAGE_SCOPE_MAP.getOrDefault(extension, "source.txt");
        this.extension = extension;
        try {
            editor.setEditorLanguage(new TML(scopeName, getActivity()));
        } catch (Throwable e) {
            Log.e("EditorFragment", "Error setting language: " + scopeName, e);
        }
    }

    private void setupColorScheme(SoraEditor editor) {
        try {
            editor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
            editor.getColorScheme().setColor(EditorColorScheme.BLOCK_LINE, Color.parseColor("#505050"));
        } catch (Exception e) {
            Log.e("EditorFragment", "Error setting color scheme", e);
        }
    }

    private void handleContentChange() {
       EditorActivity act = (EditorActivity)getActivity();
        act.updateMenu();
        if (isSaved) {
            isSaved = false;
            updateTabState();
        }
    }

    public void save() {
        if (file == null || editorView == null) return;

        String code = getCode();
        if (code != null) {
            try {
                file.write(code);
                isSaved = true;
                updateTabState();
            } catch (Exception e) {
                Log.e("EditorFragment", "Error saving file", e);
            }
        }
    }

    public String getCode() {
        return editorView != null ? editorView.getText().toString() : null;
    }

    public void setCode(String code) {
        if (editorView != null) {
            editorView.setText(code);
        }
    }

    public void updateTabState() {
        if (isActivityInvalid()) return;

        title = file.getName() + (isSaved ? "" : "*");
        mainHandler.post(() -> {
            try {
                TabLayout.Tab tab = tabRef != null ? tabRef.get() : null;
                if (tab != null && tab.view != null && !isActivityInvalid()) {
                    tab.setText(title);
                }
            } catch (Exception e) {
                Log.e("EditorFragment", "Error updating tab state", e);
            }
        });
    }

    private boolean isActivityInvalid() {
        return getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed();
    }

    @Override
    public void onDestroyView() {
        if (editorView != null) {
            if (contentChangeSubscriber != null) {
                contentChangeSubscriber.unsubscribe();
            }
            editorView.release();
        }
        editorView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        backgroundExecutor.shutdown();
        try {
            if (!backgroundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tabRef = null;
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
        this.tabRef = tab != null ? new WeakReference<>(tab) : null;
        updateTabState();
    }

    public boolean canFormat() {
        if (".html".equals(extension)) {
            if (formatter == null) formatter = new HTMLFormatter();
            return true;
        } else if (".js".equals(extension)) {
            if (formatter == null) formatter = new JSFormatter();
            return true;
        }
        return false;
    }

    public void format() {
        if (canFormat() && editorView != null) {
            setCode(formatter.format(editorView.getText().toString()));
        }
    }
}