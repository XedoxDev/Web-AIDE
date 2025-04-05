package org.xedox.webaide.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import org.xedox.webaide.editor.EditorFragment;
import org.xedox.webaide.io.IFile;

import java.util.ArrayList;
import java.util.List;

public class EditorAdapter extends FragmentStateAdapter {

    private final List<EditorFragment> fragments = new ArrayList<>();
    private final FragmentActivity activity;

    public EditorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void addFile(IFile file) {
        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i).getFile().getFullPath().equals(file.getFullPath())) {
                return;
            }
        }
        fragments.add(EditorFragment.newInstance(file));
        notifyItemInserted(fragments.size() - 1);
    }

    public void removeFile(int position) {
        if (position >= 0 && position < fragments.size()) {
            fragments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void saveAll() {
        for (int i = 0; i < getItemCount(); i++) {
            fragments.get(i).save();
        }
    }

    public List<IFile> getFiles() {
        List<IFile> list = new ArrayList<>();
        fragments.forEach(
                (ef) -> {
                    list.add(ef.getFile());
                });
        return list;
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
    }

    public int findFilePosition(IFile file) {
        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i).getFile().getFullPath().equals(file.getFullPath())) {
                return i;
            }
        }
        return -1;
    }

    public List<EditorFragment> getFragments() {
        return this.fragments;
    }
}
