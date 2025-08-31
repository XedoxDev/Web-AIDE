package org.xedox.webaide.editor.drawer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.xedox.filetree.adapter.FileTreeAdapter;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.utils.BaseFragment;
import org.xedox.webaide.AppCore;
import org.xedox.webaide.R;

public class FileTreeFragment extends BaseFragment {

    private static final String ARG_PATH = "file_tree_path";

    private FileTreeView fileTree;
    private SwipeRefreshLayout swipeRefresh;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String currentPath;

    @Override
    public String getTitle() {
        return AppCore.string(R.string.file_tree);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_tree, null, false);
        currentPath = getArguments().getString(ARG_PATH);
        fileTree = view.findViewById(R.id.file_tree);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::refreshFileTree);
        setupTree();
        return view;
    }
    
    private void setupTree() {
    	fileTree.loadPath(currentPath);
        fileTree.setIcon(".css", R.drawable.css);
        fileTree.setIcon(".html", R.drawable.html);
        fileTree.setIcon(".png", R.drawable.image);
        fileTree.setIcon(".js", R.drawable.javascript);
        fileTree.setIcon(".jpg", R.drawable.image);
        fileTree.setIcon(".jpeg", R.drawable.image);
        fileTree.setIcon(".json", R.drawable.json);
    }

    private void refreshFileTree() {
        handler.post(
                () -> {
                    fileTree.refresh();
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    public static FileTreeFragment newInstance(String path) {
        FileTreeFragment fragment = new FileTreeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public FileTreeView getFileTree() {
        return fileTree;
    }
}
