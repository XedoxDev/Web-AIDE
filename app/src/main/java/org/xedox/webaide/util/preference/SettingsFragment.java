package org.xedox.webaide.util.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import org.xedox.webaide.IDE;
import org.xedox.webaide.R;

public class SettingsFragment extends PreferenceFragmentCompat 
    implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
        
        ListPreference themePreference = findPreference("theme");
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener(this);
            updateThemePreferenceSummary(themePreference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        
        if (key.equals("theme")) {
            IDE.applyTheme(getActivity(), (String) newValue);
            updateThemePreferenceSummary((ListPreference) preference);
            return true;
        }
        return false;
    }

    private void updateThemePreferenceSummary(ListPreference preference) {
        String value = preference.getValue();
        CharSequence entry = preference.getEntry();
        preference.setSummary(entry != null ? entry : "");
    }

    @Override
    public void onResume() {
        super.onResume();
        ListPreference themePreference = findPreference("theme");
        if (themePreference != null) {
            updateThemePreferenceSummary(themePreference);
        }
    }
}