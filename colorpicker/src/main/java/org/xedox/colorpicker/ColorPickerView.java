package org.xedox.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

public class ColorPickerView extends RelativeLayout {

    private TextInputEditText hexInput, redInput, greenInput, blueInput;
    private ColorSquareView colorSquare;
    private ColorLineView colorLine;
    private View selectedColorView;
    private boolean isUpdating = false;

    public ColorPickerView(@NonNull Context context) {
        super(context);
        init();
    }

    public ColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.color_picker_layout, this);

        colorSquare = findViewById(R.id.color_square);
        colorLine = findViewById(R.id.color_line);
        selectedColorView = findViewById(R.id.selected_color);
        hexInput = findViewById(R.id.hex_value);
        redInput = findViewById(R.id.r_value);
        greenInput = findViewById(R.id.g_value);
        blueInput = findViewById(R.id.b_value);

        redInput.setHint("0");
        greenInput.setHint("0");
        blueInput.setHint("0");
        hexInput.setHint("000000");

        setSelectedColor(Color.RED);

        colorSquare.setOnColorSelectedListener(this::handleColorChange);
        colorLine.setOnColorSelectedListener(color -> {
            colorSquare.updateColor(color);
            handleColorChange(color);
        });

        TextWatcher rgbTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                try {
                    int r = getIntFromText(redInput);
                    int g = getIntFromText(greenInput);
                    int b = getIntFromText(blueInput);
                    handleColorChange(Color.rgb(clamp(r), clamp(g), clamp(b)));
                } catch (Exception ignored) {}
            }
        };

        redInput.addTextChangedListener(rgbTextWatcher);
        greenInput.addTextChangedListener(rgbTextWatcher);
        blueInput.addTextChangedListener(rgbTextWatcher);

        hexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                try {
                    String hex = s.toString().replace("#", "").trim();
                    if (hex.length() == 6) {
                        handleColorChange(Color.parseColor("#" + hex));
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private void handleColorChange(@ColorInt int color) {
        if (isUpdating) return;
        isUpdating = true;
        
        int hexSelection = hexInput.getSelectionStart();
        int redSelection = redInput.getSelectionStart();
        int greenSelection = greenInput.getSelectionStart();
        int blueSelection = blueInput.getSelectionStart();

        selectedColorView.setBackgroundColor(color);
        colorSquare.updateColor(color);
        colorLine.setSelectedColor(color);

        updateTextFieldWithoutReplacing(hexInput, String.format("%06X", (0xFFFFFF & color)), hexSelection);
        updateTextFieldWithoutReplacing(redInput, getTextForRgbField(Color.red(color)), redSelection);
        updateTextFieldWithoutReplacing(greenInput, getTextForRgbField(Color.green(color)), greenSelection);
        updateTextFieldWithoutReplacing(blueInput, getTextForRgbField(Color.blue(color)), blueSelection);

        isUpdating = false;
    }

    private String getTextForRgbField(int value) {
        return value == 0 ? "" : String.valueOf(value);
    }

    private void updateTextFieldWithoutReplacing(TextInputEditText editText, String newValue, int selection) {
        if (!editText.getText().toString().equals(newValue)) {
            editText.setText(newValue);
            if (selection >= 0) {
                editText.setSelection(Math.min(selection, newValue.length()));
            }
        }
    }

    private int getIntFromText(TextInputEditText editText) {
        try {
            String text = editText.getText().toString();
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @ColorInt
    public int getSelectedColor() {
        return ((ColorDrawable) selectedColorView.getBackground()).getColor();
    }

    public void setSelectedColor(@ColorInt int color) {
        handleColorChange(color);
    }

    public String getColorRgb() {
        int col = getSelectedColor();
        return String.format("(%d, %d, %d)", Color.red(col), Color.green(col), Color.blue(col));
    }

    public String getColorHex() {
        int col = getSelectedColor();
        return String.format("#%06X", (0xFFFFFF & col));
    }
}