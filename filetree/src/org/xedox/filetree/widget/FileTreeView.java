package org.xedox.filetree.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.xedox.filetree.utils.Node;

public class FileTreeView extends RecyclerView {

    public FileTreeAdapter adapter;
    
    public FileTreeView(Context context) {
        super(context, null);
        initialize();
    }

    public FileTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        adapter = new FileTreeAdapter(getContext());
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(adapter);
    }

    public void loadPath(String path) {
        Node root = new Node(path);
        adapter.setRoot(root);
    }

    public void shutdown() {
        //adapter.shutdown();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        shutdown();
    }

    public Node getRoot() {
        return adapter.getRoot();
    }
}
