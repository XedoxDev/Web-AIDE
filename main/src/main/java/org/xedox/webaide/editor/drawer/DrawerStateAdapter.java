package org.xedox.webaide.editor.drawer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;
import org.xedox.utils.BaseFragment;

public class DrawerStateAdapter extends FragmentStateAdapter {

    private final List<BaseFragment> fragments = new ArrayList<>();

    public DrawerStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
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

    public void addFragment(BaseFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
    }

    public void addFragment(int position, BaseFragment fragment) {
        fragments.add(position, fragment);
        notifyItemInserted(position);
    }

    public void removeFragment(BaseFragment fragment) {
        int position = fragments.indexOf(fragment);
        if (position != -1) {
            fragments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            fragments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public BaseFragment getFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        }
        return null;
    }

    public List<BaseFragment> getFragments() {
        return new ArrayList<>(fragments);
    }

    public void clearFragments() {
        int size = fragments.size();
        fragments.clear();
        notifyItemRangeRemoved(0, size);
    }
}
