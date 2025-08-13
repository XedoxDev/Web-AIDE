package org.xedox.webaide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleBackPressed() {
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        public SettingsFragment() {}

        @Override
        public void onCreatePreferences(Bundle extraArgs, String root) {
            setPreferencesFromResource(R.xml.settings_preferences, root);
            Preference appThemePref = findPreference("app_theme");
            Preference appDialogTypePref = findPreference("app_dialog_type");
            if (appThemePref != null) {
                appThemePref.setOnPreferenceChangeListener(this);
            }
            if (appDialogTypePref != null) {
                appDialogTypePref.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String key = pref.getKey();
            SharedPreferences sp =
                        PreferenceManager.getDefaultSharedPreferences(requireContext());
                        sp.edit().putString(key, newValue.toString()).apply();
            if (key.equals("app_theme")) {
                AppCore.setAppDelegate(newValue.toString());
                return true;
            } else if (key.equals("app_dialog_type")) {
                AppCore.setDialogType(newValue.toString());
                return true;
            }
            return false;
        }
    }
}
