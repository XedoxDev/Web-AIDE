package org.xedox.filetree.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import org.xedox.filetree.R;
import org.xedox.filetree.utils.Node;
import java.util.List;
import static org.xedox.filetree.adapter.FileTreeAdapter.*;
import org.xedox.filetree.adapter.FileTreeAdapter;

public class FileTreeView extends RecyclerView {

    private FileTreeAdapter adapter;

    public FileTreeView(Context context) {
        super(context);
        initialize(context, null);
    }

    public FileTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public FileTreeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        adapter = new FileTreeAdapter(context);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(adapter);
        setHasFixedSize(false);
    }

    public void loadPath(String path) {
        adapter.loadPath(path);
    }

    public void loadPath(File path) {
        adapter.loadFile(path);
    }

    public void addNode(Node node) {
        adapter.add(node);
    }

    public void addNode(int position, Node node) {
        adapter.add(position, node);
    }

    public void addNodes(List<Node> nodes) {
        adapter.addAll(nodes);
    }

    public void removeNode(Node node) {
        adapter.remove(node);
    }

    public void removeNode(int position) {
        adapter.remove(position);
    }

    public void updateNode(Node node) {
        adapter.update(node);
    }
    
    public void updateNode(Node node, Node newNode) {
        adapter.update(node, newNode);
    }

    public void clearNodes() {
        adapter.clear();
    }

    public Node getNode(int position) {
        return adapter.getNode(position);
    }

    public int getNodePosition(Node node) {
        return adapter.indexOf(node);
    }

    public List<Node> getAllNodes() {
        return adapter.getNodes();
    }

    public void setNodes(List<Node> nodes) {
        adapter.setNodes(nodes);
    }

    public void moveNode(int fromPosition, int toPosition) {
        adapter.move(fromPosition, toPosition);
    }

    public void openFolder(Node node) {
        adapter.openFolder(node);
    }

    public void closeFolder(Node node) {
        adapter.closeFolder(node);
    }

    public void toggleFolder(Node node) {
        if (node.isOpen()) {
            closeFolder(node);
        } else {
            openFolder(node);
        }
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        adapter.setOnFileClickListener(listener);
    }

    public void setOnFileLongClickListener(OnFileLongClickListener listener) {
        adapter.setOnFileLongClickListener(listener);
    }

    public void setFileItemLayout(int layoutResId) {
        adapter.setFileItemLayout(layoutResId);
    }

    public void setIndent(int indentPx) {
        adapter.setIndent(indentPx);
    }

    public void setIcon(String endsWith, int iconResId) {
        adapter.setIcon(endsWith, iconResId);
    }

    public void refresh() {
        adapter.refresh();
    }
    
    public int indexOfNode(Node node) {
    	return adapter.indexOf(node);
    }

    public void scrollToNode(Node node) {
        int position = adapter.indexOf(node);
        if (position != -1) {
            scrollToPosition(position);
        }
    }

    public void smoothScrollToNode(Node node) {
        int position = adapter.indexOf(node);
        if (position != -1) {
            smoothScrollToPosition(position);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        adapter.shutdown();
    }
}
