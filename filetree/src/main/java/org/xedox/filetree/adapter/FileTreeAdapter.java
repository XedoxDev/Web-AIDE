package org.xedox.filetree.adapter;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.R;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.VH> {

    private Context context;
    private List<Node> nodes = new ArrayList<>();
    private Node root;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private int fileItemLayout = R.layout.file_item;
    private int indent;
    private Map<String, Integer> iconMapping = new HashMap<>();
    private OnFileClickListener onFileClickListener;
    private OnFileLongClickListener onFileLongClickListener;

    public FileTreeAdapter(Context context) {
        this.context = context;
        this.indent = (int) (40 * context.getResources().getDisplayMetrics().density);
        setIcon("folder", R.drawable.folder);
        setIcon("folder_hidded", R.drawable.folder_hidden);
        setIcon("folder_open", R.drawable.folder_open);
        setIcon("base_file", R.drawable.file);
        setIcon("base_file_hidded", R.drawable.file_hidden);
    }

    public void setIcon(String endsWith, int id) {
        iconMapping.put(endsWith, id);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(fileItemLayout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Node node = nodes.get(position);
        holder.name.setText(node.getName());
        holder.item.setX(node.getLevel() * indent);
        if (node.isFile()) {
            holder.item.setOnClickListener((v) -> onFileClick(holder, node));
            holder.item.setOnLongClickListener(
                    (v) -> {
                        onFileLongClick(holder, node);
                        return true;
                    });
            boolean findIcon = false;
            for (String endsWith : iconMapping.keySet()) {
                if (node.getName().endsWith(endsWith)) {
                    holder.icon.setImageResource(iconMapping.get(endsWith));
                    findIcon = true;
                } else if (node.getName().startsWith(".")) {
                    holder.icon.setImageResource(iconMapping.get("base_file_hidded"));
                    findIcon = true;
                }
            }
            if (!findIcon) {
                holder.icon.setImageResource(iconMapping.get("base_file"));
            }
        } else {
            holder.item.setOnClickListener((v) -> onFolderClick(holder, node));
            if (node.isOpen()) {
                holder.icon.setImageResource(iconMapping.get("folder_open"));
            } else if (node.getName().startsWith(".")) {
                holder.icon.setImageResource(iconMapping.get("folder_hidded"));
            } else {
                holder.icon.setImageResource(iconMapping.get("folder"));
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public void shutdown() {
        executor.shutdown();
    }

    public int getFileItemLayout() {
        return fileItemLayout;
    }

    public void setFileItemLayout(int fileItemLayout) {
        this.fileItemLayout = fileItemLayout;
        notifyDataSetChanged();
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final View item;
        final ImageView icon;
        final TextView name;

        public VH(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.parent);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
        }
    }

    private void onFileClick(VH holder, Node node) {
        if (onFileClickListener != null) onFileClickListener.onFileClick(node);
    }

    private void onFileLongClick(VH holder, Node node) {
        if (onFileLongClickListener != null) onFileLongClickListener.onFileLongClick(node);
    }

    private void onFolderClick(VH holder, Node node) {
        int position = nodes.indexOf(node);
        if (!node.isOpen()) {
            openFolder(node);
        } else {
            closeFolder(node);
        }
        notifyItemChanged(position);
    }

    public void openFolder(Node node) {
        node.setOpen(true);
        if (!node.isDirectory() || node.list() == null || node.list().length == 0) {
            return;
        }

        int start = nodes.indexOf(node);
        if (start == -1) return;

        List<Node> toAdd = new ArrayList<>();
        File[] files = node.listFiles();
        if (files != null) {
            for (File file : files) {
                toAdd.add(new Node(file));
            }
            Collections.sort(
                    toAdd,
                    (n1, n2) -> {
                        if (n1.isDirectory() && !n2.isDirectory()) return -1;
                        if (!n1.isDirectory() && n2.isDirectory()) return 1;
                        return n1.getName().compareToIgnoreCase(n2.getName());
                    });
                    for(Node n : toAdd) {
                    	n.setLevel(node.getLevel()+1);
                    }
        }

        if (!toAdd.isEmpty()) {
            nodes.addAll(start + 1, toAdd);
            notifyItemRangeInserted(start+1, toAdd.size());
        }
    }

    public void closeFolder(Node node) {
        node.setOpen(false);
        if (node.list().length == 0) return;
        int start = nodes.indexOf(node);
        if (start == -1) return;

        int end = start + 1;
        String parentPath = node.getAbsolutePath();

        while (end < nodes.size()) {
            Node child = nodes.get(end);
            if (!child.getAbsolutePath().startsWith(parentPath)) break;
            end++;
        }

        if (end > start + 1) {
            nodes.subList(start + 1, end).clear();
            notifyItemRangeRemoved(start+1, end - start - 1);
        }
    }

    public interface OnFileClickListener {
        void onFileClick(Node node);
    }

    public interface OnFileLongClickListener {
        void onFileLongClick(Node node);
    }

    public OnFileClickListener getOnFileClickListener() {
        return this.onFileClickListener;
    }

    public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.onFileClickListener = onFileClickListener;
    }

    public OnFileLongClickListener getOnFileLongClickListener() {
        return this.onFileLongClickListener;
    }

    public void setOnFileLongClickListener(OnFileLongClickListener onFileLongClickListener) {
        this.onFileLongClickListener = onFileLongClickListener;
    }

    public void loadFile(File file) {
        nodes.clear();
        nodes.add(new Node(file));
        notifyDataSetChanged();
    }

    public void add(Node node) {
        nodes.add(node);
        notifyItemInserted(nodes.indexOf(node));
    }

    public void loadPath(String file) {
        nodes.clear();
        nodes.add(new Node(file));
        notifyDataSetChanged();
    }
}
