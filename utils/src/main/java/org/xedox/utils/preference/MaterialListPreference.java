package org.xedox.utils.preference;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.ListPreference;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.utils.R;

public class MaterialListPreference extends ListPreference {

    public MaterialListPreference(Context context) {
        super(context);
    }

    public MaterialListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        int selectedIndex = findIndexOfValue(getValue());
        
        DialogBuilder builder = new DialogBuilder(getContext());
        builder.setTitle(getTitle());
        builder.setSingleChoiceItems(getEntries(), selectedIndex, (dialog, which) -> {
            if (which >= 0 && getEntryValues() != null) {
                String value = getEntryValues()[which].toString();
                if (callChangeListener(value)) {
                    setValue(value);
                }
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}