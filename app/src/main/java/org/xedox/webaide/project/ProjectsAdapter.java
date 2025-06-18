package org.xedox.webaide.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.xedox.webaide.R;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.dialogs.RenameProjectDialog;
import org.xedox.webaide.util.OverflowMenu;
import org.xedox.webaide.util.io.IFile;

import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<Project> projects = new ArrayList<>();
    private final LayoutInflater inflater;
    private OnProjectClickListener clickListener;
    private final Context context;
    private OnChangeListener onChangeListener;

    public ProjectsAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projects.get(position));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void change() {
        if (onChangeListener != null) {
            onChangeListener.onChange();
        }
    }

    public void updateProjects(@NonNull List<Project> newProjects) {
        projects.clear();
        projects.addAll(newProjects);
        notifyDataSetChanged();
        change();
    }

    public void appendProject(@NonNull Project project) {
        projects.add(project);
        notifyItemInserted(projects.size() - 1);
    }

    public void removeProject(int position) {
        if (position >= 0 && position < projects.size()) {
            projects.remove(position);
            notifyItemRemoved(position);
        }
        change();
    }

    public void add(Project project) {
        projects.add(project);
        notifyItemInserted(getItemCount() - 1);
        change();
    }

    public void remove(Project project) {
        notifyItemRemoved(projects.indexOf(project));
        IFile f = projects.get(projects.indexOf(project)).path;
        if (f.isFile()) {
            f.remove();
        } else {
            f.removeDir();
        }
        projects.remove(project);
        change();
    }

    public void rename(int pos, String name) {
        projects.get(pos).name = name;
        notifyItemChanged(pos);
        change();
    }

    public OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onChange();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView pathView;
        private final ImageButton more;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.name);
            pathView = itemView.findViewById(R.id.path);
            more = itemView.findViewById(R.id.more);
        }

        void bind(@NonNull Project project) {
            nameView.setText(project.name);
            pathView.setText(project.path.toFile().getAbsolutePath());

            more.setOnClickListener((v) -> {
                OverflowMenu.show(context, v, R.menu.project, (item) -> {
                    int id = item.getItemId();
                    if (id == R.id.rename) {
                        RenameProjectDialog.show(context, project.name, getAbsoluteAdapterPosition());
                    } else if (id == R.id.remove) {
                        ProjectManager.removeProject(project.name);
                        remove(project);
                    } else if (id == R.id.clone) {
                        Project cloned = ProjectManager.cloneProject(project.name, (BaseActivity) context);
                        if (cloned != null) {
                            add(cloned);
                        }
                    }
                });
            });

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onProjectClick(project);
                }
            });
        }
    }
}
