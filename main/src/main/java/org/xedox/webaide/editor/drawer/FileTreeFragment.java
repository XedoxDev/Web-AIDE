package org.xedox.webaide.editor.drawer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.xedox.filetree.adapter.FileTreeAdapter;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.utils.BaseFragment;
import org.xedox.webaide.AppCore;
import org.xedox.webaide.R;

public class FileTreeFragment extends BaseFragment {

    private static final String ARG_PATH = "file_tree_path";

    private FileTreeView fileTree;
    private FileTreeAdapter adapter;
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

        adapter = new FileTreeAdapter(requireActivity());
        fileTree.setAdapter(adapter);

        fileTree.adapter.loadPath(currentPath);

        swipeRefresh.setOnRefreshListener(this::refreshFileTree);
        return view;
    }

    private void refreshFileTree() {
        handler.post(
                () -> {
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

    public FileTreeAdapter getAdapter() {
        return adapter;
    }
}
