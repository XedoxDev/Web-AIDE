package org.xedox.webaide.preference;

import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.MultiSelectListPreference;
import java.util.Set;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.HashSet;
import android.widget.EditText;
import org.xedox.webaide.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }

}
