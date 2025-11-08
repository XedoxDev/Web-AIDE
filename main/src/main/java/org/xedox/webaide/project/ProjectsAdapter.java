package org.xedox.webaide.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xedox.utils.OverflowMenu;
import org.xedox.webaide.R;
import org.xedox.webaide.dialog.RenameProjectDialog;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<Project> items = new ArrayList<>();
    private OnChangeListener onChangeListener;
    private OnProjectClickListener onProjectClickListener;
    private Context context;

    public ProjectsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.item_project, container, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        Project item = get(position);
        holder.name.setText(item.getName() != null ? item.getName() : "");
        holder.more.setOnClickListener(
                v -> OverflowMenu.show(v, R.menu.project, i -> handleMenuItem(i, position)));
        holder.itemView.setOnClickListener(
                (view) -> {
                    if (onProjectClickListener != null) onProjectClickListener.onClick(view, item);
                });
    }

    private void handleMenuItem(MenuItem item, int position) {
        int id = item.getItemId();
        if (id == R.id.remove) {
            Project project = get(position);
            if (project != null) {
                Project.removeProject(project);
                remove(position);
            }
        } else if (id == R.id.rename) {
            RenameProjectDialog.show(context, this, get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int add(Project project) {
        if (project == null) return -1;

        items.add(project);
        int position = items.size() - 1;
        notifyItemInserted(position);
        notifyChange();
        return position;
    }

    public void update(Project project) {
        int position = indexOf(project);
        if (position >= 0) {
            items.set(position, project);
            notifyItemChanged(position);
            notifyChange();
        }
    }

    public void add(int position, Project project) {
        if (project == null || position < 0 || position > items.size()) return;

        items.add(position, project);
        notifyItemInserted(position);
        notifyChange();
    }

    public Project get(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public int indexOf(Project project) {
        return project != null ? items.indexOf(project) : -1;
    }

    public void remove(int position) {
        if (position < 0 || position >= items.size()) return;

        items.remove(position);
        notifyItemRemoved(position);
        notifyChange();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
        notifyChange();
    }

    public void addAll(Collection<Project> items) {
        if (items == null) return;

        int oldSize = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(oldSize, items.size());
        notifyChange();
    }

    public void setItems(Collection<Project> items) {
        if (items == null) return;

        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
        notifyChange();
    }

    private void notifyChange() {
        if (onChangeListener != null) {
            onChangeListener.onChange(items.size() > 0);
        }
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final ImageButton more;

        public ProjectViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            more = view.findViewById(R.id.more);
        }
    }

    public interface OnChangeListener {
        void onChange(boolean hasItems);
    }

    public interface OnProjectClickListener {
        void onClick(View view, Project project);
    }

    public OnChangeListener getOnChangeListener() {
        return this.onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
        notifyChange();
    }

    @Override
    public long getItemId(int position) {
        return get(position).hashCode();
    }

    public OnProjectClickListener getOnProjectClickListener() {
        return this.onProjectClickListener;
    }

    public void setOnProjectClickListener(OnProjectClickListener onProjectClickListener) {
        this.onProjectClickListener = onProjectClickListener;
    }
}
