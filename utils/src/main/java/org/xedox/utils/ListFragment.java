package org.xedox.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListFragment<T> extends Fragment {

    private ListView listView;
    private List<T> items;
    private OnItemClickListener<T> itemClickListener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.itemClickListener = listener;
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        T item = items.get(position);
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(item);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        listView = new ListView(getActivity());
        items = new ArrayList<>();

        return listView;
    }

    @SuppressWarnings("unchecked")
    public void setItems(Collection<T> items) {
        this.items = new ArrayList<>(items);
        ArrayAdapter<T> adapter =
                new ArrayAdapter<T>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        (T[]) items.toArray());
        listView.setAdapter(adapter);
    }

    public static <T>ListFragment newInstance() {
        return new ListFragment<T>();
    }
}
