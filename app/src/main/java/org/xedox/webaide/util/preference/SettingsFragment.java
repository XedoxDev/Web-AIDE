package org.xedox.webaide.util.preference;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import org.xedox.webaide.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }

}
