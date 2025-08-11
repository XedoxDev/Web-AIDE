package org.xedox.webaide.editor;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.xedox.utils.BaseFragment;

public class EditorStateAdapter extends FragmentStateAdapter {

    private final FragmentActivity activity;
    private final List<BaseFragment> fragments = new ArrayList<>();
    private OnChangeListener onChangeListener;

    public EditorStateAdapter(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public BaseFragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return fragments.get(position).hashCode();
    }

    public void add(BaseFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.indexOf(fragment));
        notifyChange();
    }

    public void add(int position, BaseFragment fragment) {
        fragments.add(position, fragment);
        notifyItemInserted(position);
        notifyChange();
    }

    public BaseFragment remove(int position) {
        BaseFragment removed = fragments.remove(position);
        notifyItemRemoved(position);
        notifyChange();
        return removed;
    }

    public int indexOf(BaseFragment fragment) {
        return fragments.indexOf(fragment);
    }

    public <C extends BaseFragment> List<C> getAllByClass(Class<C> fragmentClass) {
        List<C> result = new ArrayList<>();
        for (BaseFragment fragment : fragments) {
            if (fragmentClass.isInstance(fragment)) {
                result.add(fragmentClass.cast(fragment));
            }
        }
        return result;
    }

    public void removeByFile(File file) {
        List<FileFragment> toRemove = new ArrayList<>();
        for (FileFragment fragment : getAllByClass(FileFragment.class)) {
            if (file.getAbsolutePath().equals(fragment.getFile().getAbsolutePath())) {
                toRemove.add(fragment);
            }
        }
        for (FileFragment fragment : toRemove) {
            remove(indexOf(fragment));
        }
    }

    @SuppressWarnings("unchecked")
    public BaseFragment get(int position) {
        if (position < 0 || position >= fragments.size()) {
            return null;
        }
        return fragments.get(position);
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
        notifyChange();
    }

    public boolean contains(BaseFragment fragment) {
        return fragments.contains(fragment);
    }

    private void notifyChange() {
        if (onChangeListener != null) {
            onChangeListener.onChange(!fragments.isEmpty());
        }
    }

    public interface OnChangeListener {
        void onChange(boolean hasItems);
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
        notifyChange();
    }
}