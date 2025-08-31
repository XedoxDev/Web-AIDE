package org.xedox.webaide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.utils.format.FormatConfig;
import org.xedox.utils.format.IFormat;

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
            
            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new SettingsFragment())
                        .commit();
            }
        } catch (Exception err) {
            ErrorDialog.show(this, err);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void handleBackPressed() {
        onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceClickListener {

        public SettingsFragment() {}

        @Override
        public void onCreatePreferences(Bundle extraArgs, String root) {
            setPreferencesFromResource(R.xml.settings_preferences, root);
            
            Preference generalPref = findPreference("general_settings");
            Preference editorPref = findPreference("editor_settings");
            
            if (generalPref != null) {
                generalPref.setOnPreferenceClickListener(this);
            }
            if (editorPref != null) {
                editorPref.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            
            if (key.equals("general_settings")) {
                navigateToFragment(new GeneralFragment());
                return true;
            } else if (key.equals("editor_settings")) {
                navigateToFragment(new EditorFragment());
                return true;
            }
            
            return false;
        }

        private void navigateToFragment(PreferenceFragmentCompat fragment) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static class GeneralFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        public GeneralFragment() {}

        @Override
        public void onCreatePreferences(Bundle extraArgs, String root) {
            setPreferencesFromResource(R.xml.settings_general, root);

            Preference appDialogTypePref = findPreference("app_dialog_type");

            if (appDialogTypePref != null) {
                appDialogTypePref.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = sp.edit();

            if (key.equals("app_dialog_type")) {
                editor.putString(key, newValue.toString()).apply();
                AppCore.setDialogType(newValue.toString());
                return true;
            }

            return false;
        }
    }

    public static class EditorFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        public EditorFragment() {}

        @Override
        public void onCreatePreferences(Bundle extraArgs, String root) {
            setPreferencesFromResource(R.xml.settings_editor, root);
            
            Preference useTabPref = findPreference("editor_use_tab");
            Preference indentSizePref = findPreference("editor_indent_size");
            
            if (useTabPref != null) {
                useTabPref.setOnPreferenceChangeListener(this);
            }
            if (indentSizePref != null) {
                indentSizePref.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = sp.edit();

            if (key.equals("editor_use_tab")) {
                boolean useTab = (Boolean) newValue;
                editor.putBoolean(key, useTab).apply();
                FormatConfig.getInstance().setUseTab(useTab);
                return true;
            } else if (key.equals("editor_indent_size")) {
                try {
                    int indentSize = Integer.parseInt(newValue.toString());
                    editor.putString(key, newValue.toString()).apply();
                    FormatConfig.getInstance().setIndentSize(indentSize);
                } catch (Exception e) {
                    ErrorDialog.show(requireActivity(), e);
                }
                return true;
            }

            return false;
        }
    }
}