package org.xedox.webaide;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;

public class OverflowMenu {

    public static void show(Context context, View view, int menuId, OnItemClickListener oicl) {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(menuId, menu.getMenu());

        menu.setOnMenuItemClickListener(
                item -> {
                    if (oicl != null) {
                        oicl.onItemClick(item);
                        return true;
                    }
                    return false;
                });

        menu.show();
    }

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }
}
