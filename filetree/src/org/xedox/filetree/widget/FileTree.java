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

public class FileTree extends RecyclerView {

    public FileTreeAdapter adapter;
    public ExecutorService service = Executors.newSingleThreadExecutor(); // Thread
    private final Handler uiHandler = new Handler(Looper.getMainLooper());  // Main loop

    public FileTree(Context context) {
        super(context, null);
        initialize();
    }

    public FileTree(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        adapter = new FileTreeAdapter(getContext());
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(adapter);
    }

    public void loadPath(String path) {
        // load tree in thread, set data in adapter...
        service.submit(
                () -> {
                    Node root = new Node(path);
                    uiHandler.post(
                            () -> { 
                                adapter.setRoot(root);
                            });
                });
    }
    
    public void shutdown() {
        // shutdown thread...
        service.shutdown();
        try {
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow(); 
            }
        } catch (InterruptedException e) {
            // error :(
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
