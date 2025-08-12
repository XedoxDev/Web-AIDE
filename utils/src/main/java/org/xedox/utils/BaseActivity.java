package org.xedox.utils;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.snackbar.Snackbar;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {
    protected final Map<Integer, Boolean> menuItemsVisibility = new HashMap<>();
    protected final Map<Integer, Boolean> menuItemsEnabled = new HashMap<>();

    @Override
    protected void onStart() {
        super.onStart();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View rootView = findViewById(android.R.id.content);
        getOnBackPressedDispatcher()
                .addCallback(
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {
                                handleBackPressed();
                            }
                        });
        applyWindowInsets(findViewById(android.R.id.content));
    }

    public void updateItemVisibility(int itemId, boolean visible) {
        menuItemsVisibility.put(itemId, visible);
        invalidateOptionsMenu();
    }
    
    public void updateItemEnabled(int itemId, boolean enabled) {
        menuItemsEnabled.put(itemId, enabled);
        invalidateOptionsMenu();
    }

    public void applyWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBarsInsets =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBarsInsets.left,
                            systemBarsInsets.top,
                            systemBarsInsets.right,
                            systemBarsInsets.bottom);

                    return insets;
                });
    }

    public boolean onCreateOptionsMenu(int menuId, Menu menu) {
        getMenuInflater().inflate(menuId, menu);
        for (Map.Entry<Integer, Boolean> entry : menuItemsVisibility.entrySet()) {
            MenuItem item = menu.findItem(entry.getKey());
            if (item != null) {
                item.setVisible(entry.getValue());
            }
        }
        for (Map.Entry<Integer, Boolean> entry : menuItemsEnabled.entrySet()) {
            MenuItem item = menu.findItem(entry.getKey());
            if (item != null) {
                item.setEnabled(entry.getValue());
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m =
                        menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                Log.e("OverflowMenu", "Error forcing menu icons to show", e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void showSnackbar(int text, int type) {
        Snackbar.make(findViewById(android.R.id.content), text, type)
                .setAction(R.string.ok, null)
                .show();
    }

    public void handleBackPressed() {}
}
