package org.xedox.filetree.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.R;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.VH> {

    private List<Node> nodes = new ArrayList<>();
    private Node root;
    private final Context context;
    private final int fileItemLayout = R.layout.file_item;
    private final int indent;
    private final Map<String, Integer> fileIconMappings = new HashMap<>();
    private int defaultFileIcon = R.drawable.file;
    private int defaultFolderIcon = R.drawable.folder;
    private int defaultFolderOpenIcon = R.drawable.folder_open;
    private int defaultHiddenFileIcon = R.drawable.file_hidden;
    private int defaultHiddenFolderIcon = R.drawable.folder_hidden;
    private OnFileClickListener onFileClickListener;
    private OnFileLongClickListener onFileLongClickListener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public FileTreeAdapter(Context context) {
        this.context = context;
        this.indent = (int) (40 * context.getResources().getDisplayMetrics().density);
        fileIconMappings.put(".png", R.drawable.file_png);
        fileIconMappings.put(".jpg", R.drawable.file_jpg);
        fileIconMappings.put(".js", R.drawable.javascript);
        fileIconMappings.put(".css", R.drawable.css);
        fileIconMappings.put(".html", R.drawable.html);
        fileIconMappings.put(".zip", R.drawable.zip);
        fileIconMappings.put(".gz", R.drawable.zip);
        fileIconMappings.put(".7z", R.drawable.zip);
        fileIconMappings.put(".apk", R.drawable.apk_install);
        fileIconMappings.put(".aab", R.drawable.apk_install);
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

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(fileItemLayout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Node node = nodes.get(position);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.parent.getLayoutParams();
        params.leftMargin = indent * node.level;
        holder.parent.setLayoutParams(params);
        holder.name.setText(node.name);

        if (node.isFile) {
            setupFileIcon(holder, node);
            holder.isOpen.setImageResource(0);
        } else {
            setupFolderIcon(holder, node);
            holder.isOpen.setImageResource(R.drawable.arrow_up);
            holder.isOpen.setRotation(node.isOpen ? 180 : 0);
        }

        holder.parent.setOnClickListener(v -> {
            if (node.isFile) {
                if (onFileClickListener != null) {
                    onFileClickListener.onClick(node, new File(node.fullPath), v);
                }
            } else {
                toggleFolder(node);
                holder.isOpen.animate().rotation(node.isOpen ? 180 : 0).start();
            }
        });

        holder.parent.setOnLongClickListener(v -> {
            if (onFileLongClickListener != null) {
                onFileLongClickListener.onClick(node, new File(node.fullPath), v);
                return true;
            }
            return false;
        });
    }

    private void setupFileIcon(VH holder, Node node) {
        if (node.name.startsWith(".")) {
            holder.icon.setImageResource(defaultHiddenFileIcon);
            return;
        }
        for (Map.Entry<String, Integer> entry : fileIconMappings.entrySet()) {
            if (node.name.endsWith(entry.getKey())) {
                holder.icon.setImageResource(entry.getValue());
                return;
            }
        }
        holder.icon.setImageResource(defaultFileIcon);
    }

    private void setupFolderIcon(VH holder, Node node) {
        if (node.name.startsWith(".")) {
            holder.icon.setImageResource(defaultHiddenFolderIcon);
        } else {
            holder.icon.setImageResource(node.isOpen ? defaultFolderOpenIcon : defaultFolderIcon);
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

    public void toggleFolder(Node node) {
        node.isOpen = !node.isOpen;
        node.updateChildren();
        if (node.isOpen) {
            openFolder(node);
        } else {
            closeFolder(node);
        }
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
        executor.execute(() -> {
            int index = nodes.indexOf(node);
            if (index == -1) return;
            node.updateChildren();
            count = 0;
            addChildren(node);
            handler.post(() -> notifyItemRangeInserted(index + 1, count));
        });
    }

    public void closeFolder(Node node) {
        executor.execute(() -> {
            int parentIndex = nodes.indexOf(node);
            int removeCount = 0;
            int i = parentIndex + 1;
            while (i < nodes.size() && nodes.get(i).level > node.level) {
                removeCount++;
                i++;
            }
            if (removeCount > 0) {
                List<Node> removed = new ArrayList<>(nodes.subList(parentIndex + 1, parentIndex + 1 + removeCount));
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
        executor.shutdown();
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }

    public void setOnFileLongClickListener(OnFileLongClickListener listener) {
        this.onFileLongClickListener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        final View parent;
        final ImageView icon;
        final TextView name;
        final ImageView isOpen;

        public VH(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            isOpen = itemView.findViewById(R.id.isOpen);
        }
    }

    public interface OnFileClickListener {
        void onClick(Node node, File file, View v);
    }

    public interface OnFileLongClickListener {
        void onClick(Node node, File file, View v);
    }
}