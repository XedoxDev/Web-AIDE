package org.xedox.filetree.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import static androidx.recyclerview.widget.RecyclerView.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.R;

public class FileTreeAdapter extends Adapter<FileTreeAdapter.VH> {

    private List<Node> nodes = new ArrayList<>();
    private Node root;
    private Context context;
    private int fileItemLayout = R.layout.file_item;
    private int indent = 40;

    private Map<String, Integer> fileIconMappings = new HashMap<>();
    private int defaultFileIcon = R.drawable.file;
    private int defaultFolderIcon = R.drawable.folder;
    private int defaultFolderOpenIcon = R.drawable.folder_open;
    private int defaultHiddenFileIcon = R.drawable.file_hidden;
    private int defaultHiddenFolderIcon = R.drawable.folder_hidden;

    private OnFileClickListener onFileClickListener;
    private OnFileLongClickListener onFileLongClickListener;

    private ExecutorService exexutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    public FileTreeAdapter(Context context) {
        this.context = context;
        fileIconMappings.put(".png", R.drawable.file_png);
        fileIconMappings.put(".jpg", R.drawable.file_jpg);
        fileIconMappings.put(".js", R.drawable.javascript);
        fileIconMappings.put(".css", R.drawable.css);
        fileIconMappings.put(".html", R.drawable.html);
    }

    public void addIcon(String pattern, int drawableResId) {
        fileIconMappings.put(pattern, drawableResId);
    }

    public void removeIcon(String pattern) {
        fileIconMappings.remove(pattern);
    }

    public void setDefaultFileIcon(int drawableResId) {
        defaultFileIcon = drawableResId;
    }

    public void setDefaultFolderIcon(int drawableResId) {
        defaultFolderIcon = drawableResId;
    }

    public void setDefaultFolderOpenIcon(int drawableResId) {
        defaultFolderOpenIcon = drawableResId;
    }

    public void setDefaultHiddenFileIcon(int drawableResId) {
        defaultHiddenFileIcon = drawableResId;
    }

    public void setDefaultHiddenFolderIcon(int drawableResId) {
        defaultHiddenFolderIcon = drawableResId;
    }

    static class VH extends ViewHolder {
        ImageView isOpen;
        ImageView icon;
        TextView name;
        View parent;

        public VH(View root) {
            super(root);
            parent = root.findViewById(R.id.parent);
            icon = root.findViewById(R.id.icon);
            name = root.findViewById(R.id.name);
            isOpen = root.findViewById(R.id.isOpen);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup vg, int pos) {
        View root = LayoutInflater.from(context).inflate(fileItemLayout, vg, false);
        return new VH(root);
    }

    @Override
    public void onBindViewHolder(VH vh, int pos) {
        if (pos >= 0 && pos < nodes.size()) {
            Node node = nodes.get(pos);
            vh.parent.setTranslationX(indent * node.level);
            vh.name.setText(node.name);

            if (node.isFile) {
                initFile(vh, node);
            } else {
                initFolder(vh, node);
            }

            setupClickListeners(vh, node);
        }
    }

    private void initFile(VH vh, Node node) {
        vh.isOpen.setImageResource(0);

        if (node.name.startsWith(".")) {
            vh.icon.setImageResource(defaultHiddenFileIcon);
            return;
        }

        for (Map.Entry<String, Integer> entry : fileIconMappings.entrySet()) {
            if (node.name.endsWith(entry.getKey()) || node.name.equals(entry.getKey())) {
                vh.icon.setImageResource(entry.getValue());
                return;
            }
        }

        vh.icon.setImageResource(defaultFileIcon);
    }

    private void initFolder(VH vh, Node node) {
        vh.isOpen.setImageResource(R.drawable.arrow_up);
        vh.isOpen.setRotation(0);
        
        if (node.name.startsWith(".")) {
            vh.icon.setImageResource(defaultHiddenFolderIcon);
        } else {
            vh.icon.setImageResource(node.isOpen ? defaultFolderOpenIcon : defaultFolderIcon);
        }
    }

    private void setupClickListeners(VH vh, Node node) {
        vh.parent.setOnClickListener(
                v -> {
                    if (node.isFile) {
                        if (onFileClickListener != null) {
                            onFileClickListener.onClick(node, new File(node.fullPath), v);
                        }
                    } else {
                        toggleFolder(node);
                        vh.isOpen.animate().rotation(node.isOpen ? 180 : 0).start();
                    }
                });

        vh.parent.setOnLongClickListener(
                v -> {
                    if (onFileLongClickListener != null) {
                        onFileLongClickListener.onClick(node, new File(node.fullPath), v);
                        return true;
                    }
                    return false;
                });
    }

    private void toggleFolder(Node node) {
        node.isOpen = !node.isOpen;
        node.updateChildren();
        if (node.isOpen) {
            openFolder(node);
        } else {
            closeFolder(node);
        }
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public void setRoot(Node newRoot) {
        this.root = newRoot;
        if (root != null) {
            root.isOpen = false;
            root.level = 0;
            nodes.clear();
            nodes.add(root);
            notifyDataSetChanged();
        }
    }

    public interface OnFileClickListener {
        void onClick(Node node, File file, View v);
    }

    public interface OnFileLongClickListener {
        void onClick(Node node, File file, View v);
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }

    public void setOnFileLongClickListener(OnFileLongClickListener listener) {
        this.onFileLongClickListener = listener;
    }

    private int count;

    private void addChildren(Node node) {
        int index = nodes.indexOf(node);
        for (Node child : node.children()) {
            nodes.add(index + 1, child);
            count++;
            if (!child.isFile && child.isOpen) addChildren(child);
        }
    }

    public void openFolder(Node node) {
        exexutor.execute(
                () -> {
                    int index = nodes.indexOf(node);
                    if (index == -1) return;
                    node.updateChildren();
                    count = 0;
                    addChildren(node);

                    handler.post(() -> notifyItemRangeInserted(index + 1, count));
                });
    }

    public void closeFolder(Node node) {
        exexutor.execute(
                () -> {
                    int parentIndex = nodes.indexOf(node);
                    int removeCount = 0;
                    int i = parentIndex + 1;
                    while (i < nodes.size() && nodes.get(i).level > node.level) {
                        removeCount++;
                        i++;
                    }

                    if (removeCount > 0) {
                        List<Node> removed =
                                new ArrayList<>(
                                        nodes.subList(
                                                parentIndex + 1, parentIndex + 1 + removeCount));
                        nodes.removeAll(removed);
                        final int count = removeCount;
                        handler.post(() -> notifyItemRangeRemoved(parentIndex + 1, count));
                    }

                    node.isOpen = false;
                });
    }

    public void renameNode(Node node, String name) {
        node.name = name;
        notifyItemChanged(nodes.indexOf(node));
    }

    public void deleteNode(Node node) {
        int parentIndex = nodes.indexOf(node);
        if (parentIndex == -1) return;

        List<Node> nodesToRemove = new ArrayList<>();
        collectChildren(node, nodesToRemove);
        nodesToRemove.add(node);
        if (!nodesToRemove.isEmpty()) {
            nodes.removeAll(nodesToRemove);
            notifyItemRangeRemoved(parentIndex, nodesToRemove.size());
        }
        node.isOpen = false;
    }

    private void collectChildren(Node node, List<Node> nodesToRemove) {
        for (Node child : node.children()) {
            nodesToRemove.add(child);
            if (!child.isFile && child.isOpen) {
                collectChildren(child, nodesToRemove);
            }
        }
    }

    public void addNode(Node parent, Node toAdd) {
        if (parent == null || toAdd == null) return;

        toAdd.level = parent.level + 1;
        parent.children.add(toAdd);

        if (parent.isOpen) {
            int parentPos = nodes.indexOf(parent);
            if (parentPos == -1) return;

            int insertPos = parentPos + 1;
            while (insertPos < nodes.size() && nodes.get(insertPos).level > parent.level) {
                insertPos++;
            }

            nodes.add(insertPos, toAdd);
            notifyItemInserted(insertPos);
        }
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
        notifyDataSetChanged();
    }

    public Node getRoot() {
        return this.root;
    }

    public void shutdown() {
        exexutor.shutdown();
    }
}
