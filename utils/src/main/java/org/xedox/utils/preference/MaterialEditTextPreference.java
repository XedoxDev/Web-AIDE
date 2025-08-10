package org.xedox.utils.preference;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.preference.EditTextPreference;
import org.xedox.utils.dialog.DialogBuilder;
import android.util.TypedValue;

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
        int padding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            16, 
            getContext().getResources().getDisplayMetrics()
        );
        
        TextInputLayout textInputLayout = new TextInputLayout(getContext());
        TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());
        editText.setText(getText());
        textInputLayout.addView(editText);
        textInputLayout.setPadding(padding, 0, padding, 0);
        
        DialogBuilder dialogBuilder = new DialogBuilder(getContext());
        dialogBuilder.setTitle(getTitle());
        dialogBuilder.setView(textInputLayout); 
        dialogBuilder.setPositiveButton(getPositiveButtonText(), (dialog, which) -> {
            String value = editText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
            dialog.dismiss();
        });
        dialogBuilder.setNegativeButton(getNegativeButtonText(), (d, w) -> d.dismiss());
        dialogBuilder.show();
    }
}