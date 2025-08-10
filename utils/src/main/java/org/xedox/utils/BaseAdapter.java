package org.xedox.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> 
    extends RecyclerView.Adapter<VH> {

    public abstract void add(T item);

    public abstract void add(int position, T item);

    public abstract void remove(T item);

    public abstract void remove(int position);

    public abstract void update(int position, T item);

    public abstract void clear();

    public abstract void addAll(List<T> items);

}