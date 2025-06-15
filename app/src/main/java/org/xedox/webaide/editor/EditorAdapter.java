package org.xedox.webaide.editor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.xedox.webaide.util.io.IFile;

import java.util.ArrayList;
import java.util.List;

public class EditorAdapter extends FragmentStateAdapter {
    
    private List<EditorFragment> fragments = new ArrayList<>();
    private FragmentActivity activity;
    private OnChangeListener onChangeListener;
    
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
        fragments.add(EditorFragment.newInstance(file));
        notifyItemInserted(fragments.size() - 1);
        if (onChangeListener != null) {
            onChangeListener.onChange();
        }
    }

    public void change() {
        if (onChangeListener != null) {
            onChangeListener.onChange();
        }
    }

    public void removeFile(IFile file) {
        int position = findFilePosition(file);
        if (position >= 0) {
            fragments.remove(position);
            notifyItemRemoved(position);
            if (onChangeListener != null) {
                onChangeListener.onChange();
            }
        }
    }
    
    public void removeFile(int position) {
        if (position >= 0) {
            fragments.remove(position);
            notifyItemRemoved(position);
            if (onChangeListener != null) {
                onChangeListener.onChange();
            }
        }
    }

    public void saveAll() {
        for (EditorFragment fragment : fragments) {
            fragment.save();
        }
    }

    public List<IFile> getFiles() {
        List<IFile> list = new ArrayList<>();
        fragments.forEach(ef -> list.add(ef.getFile()));
        return list;
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
        return fragments;
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
    
    public void clear() {
    	fragments.clear();
        notifyDataSetChanged();
        change();
    }
}