package org.xedox.webaide;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.appbar.MaterialToolbar;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;

public class SettingsActivity extends BaseActivity {

    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_settings);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new SettingsFragment())
                    .commit();
        } catch (Exception err) {
            ErrorDialog.show(this, err);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SettingsFragment() {}

        @Override
        public void onCreatePreferences(Bundle extraArgs, String root) {
            setPreferencesFromResource(R.xml.settings_preferences, root);
        }
    }
}
