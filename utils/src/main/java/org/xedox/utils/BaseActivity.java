package org.xedox.utils;

import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.snackbar.Snackbar;

public class BaseActivity extends AppCompatActivity {

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

    public void showSnackbar(int text, int type) {
        Snackbar.make(findViewById(android.R.id.content), text, type)
                .setAction(R.string.ok, null)
                .show();
    }

    public void handleBackPressed() {}
}
