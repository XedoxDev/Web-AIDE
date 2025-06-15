package org.xedox.webaide.util.preference;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.MultiSelectListPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Set;
import java.util.HashSet;

public class MaterialMultiSelectListPreference extends MultiSelectListPreference {

    public MaterialMultiSelectListPreference(Context context) {
        super(context);
    }

    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        Set<String> selectedValues = getValues();
        boolean[] checkedItems = new boolean[getEntries().length];

        for (int i = 0; i < getEntryValues().length; i++) {
            checkedItems[i] = selectedValues.contains(getEntryValues()[i].toString());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(getTitle());
        builder.setMultiChoiceItems(getEntries(), checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Set<String> newValues = new HashSet<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    newValues.add(getEntryValues()[i].toString());
                }
            }
            if (callChangeListener(newValues)) {
                setValues(newValues);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}