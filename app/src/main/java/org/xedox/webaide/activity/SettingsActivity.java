package org.xedox.webaide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import android.widget.EditText;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import androidx.preference.PreferenceFragmentCompat;
import org.xedox.webaide.R;
import org.xedox.webaide.dialogs.DialogBuilder;
import org.xedox.webaide.util.preference.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    private FrameLayout contentFrame;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        loadToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        contentFrame = findViewById(R.id.content);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(contentFrame.getId(), new SettingsFragment())
                .commit();

        name = getIntent().getStringExtra("project_name");

        getOnBackPressedDispatcher()
                .addCallback(
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                finish();
                            }
                        });
    }

    @Override
    public void finish() {
        Intent i = null;
        if (name != null) {
            i =
                    new Intent(this, EditorActivity.class)
                            .addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("project_name", name);
        } else {
            i =
                    new Intent(this, MainActivity.class)
                            .addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(i);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
