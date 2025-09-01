package org.xedox.webaide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.utils.format.FormatConfig;
import org.xedox.utils.format.IFormat;
import org.xedox.utils.preference.MaterialListPreference;
import org.xedox.webaide.dialog.CopyAssetsDialog;
import org.xedox.webaide.sora.SoraEditorManager;

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
            handleBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
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
            requireActivity()
                    .getSupportFragmentManager()
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
            Preference recopyAssetsPref = findPreference("recopy_assets");
            recopyAssetsPref.setOnPreferenceClickListener(
                    (v) -> {
                        CopyAssetsDialog.show(requireActivity());
                        return true;
                    });
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
            MaterialListPreference fontsPref = findPreference("editor_font");
            try {
                List<File> fontSources = Arrays.asList(AppCore.fontList());
                String[] entries = new String[fontSources.size()];
                String[] values = new String[fontSources.size()];

                for (int i = 0; i < fontSources.size(); i++) {
                    File fontSource = fontSources.get(i);

                    File fontFile = fontSource;
                    entries[i] = fontFile.getName();
                    values[i] = fontFile.getAbsolutePath();
                }

                fontsPref.setEntries(entries);
                fontsPref.setEntryValues(values);
            } catch (Exception err) {
                ErrorDialog.show(requireActivity(), err);
            }
            MaterialListPreference themesPref = findPreference("editor_theme");
            try {
                SoraEditorManager.Theme[] themes = SoraEditorManager.getThemesList();
                String[] names = new String[themes.length];
                for (int i = 0; i < themes.length; i++) {
                    SoraEditorManager.Theme theme = themes[i];
                    names[i] = theme.name;
                }
                themesPref.setEntries(names);
                themesPref.setEntryValues(names);
            } catch (Exception err) {
                ErrorDialog.show(requireActivity(), err);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = sp.edit();
            String value = newValue.toString();
            if (key.equals("editor_use_tab")) {
                boolean useTab = (Boolean) newValue;
                editor.putBoolean(key, useTab).apply();
                FormatConfig.getInstance().setUseTab(useTab);
                return true;
            } else if (key.equals("editor_font")) {
                editor.putString(key, value).apply();
                SoraEditorManager.updateFont(value);
                return true;
            } else if (key.equals("editor_theme")) {
                editor.putString(key, value).apply();
                SoraEditorManager.updateTheme(value);
                return true;
            } else if (key.equals("editor_indent_size")) {
                try {
                    int indentSize = Integer.parseInt(value);
                    editor.putString(key, value).apply();
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
