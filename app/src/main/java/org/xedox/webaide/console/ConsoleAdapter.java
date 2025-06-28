package org.xedox.webaide.console;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConsoleAdapter extends FragmentStateAdapter {

    private final List<ConsoleFragment> fragments;

    public ConsoleAdapter(FragmentActivity context, List<ConsoleFragment> fragments) {
        super(context);
        this.fragments = new ArrayList<>(fragments);
    }

    public ConsoleAdapter(FragmentActivity context) {
        this(context, new ArrayList<>());
    }

    public void addFragment(ConsoleFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
    }

    public void removeFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            fragments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setFragments(List<ConsoleFragment> fragments) {
        this.fragments.clear();
        this.fragments.addAll(fragments);
        notifyDataSetChanged();
    }

    public List<Fragment> getFragments() {
        return new ArrayList<>(fragments);
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

    @Override
    public long getItemId(int position) {
        return fragments.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Fragment fragment : fragments) {
            if (fragment.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }

    public ConsoleFragment get(int pos) {
        return fragments.get(pos);
    }

    public ConsoleFragment getByName(String name) {
        for (ConsoleFragment f : fragments) {
            if (f.getName().equals(name)) return f;
        }
        return null;
    }
}
