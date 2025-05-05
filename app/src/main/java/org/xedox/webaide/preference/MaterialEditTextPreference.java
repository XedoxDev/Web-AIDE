package org.xedox.webaide.preference;

import android.widget.EditText;
import androidx.preference.EditTextPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MaterialEditTextPreference extends EditTextPreference {

    public MaterialEditTextPreference(android.content.Context context) {
        super(context);
    }

    public MaterialEditTextPreference(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialEditTextPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialEditTextPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        EditText editText = new EditText(getContext());
        editText.setText(getText());

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext());
        dialogBuilder.setTitle(getTitle());
        dialogBuilder.setView(editText);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), (dialog, which) -> {
            String value = editText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        });
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.show();
    }
}