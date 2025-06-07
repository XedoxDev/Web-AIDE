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
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputEditText;

public class ColorPickerView extends RelativeLayout {

    private TextInputEditText hexInput, redInput, greenInput, blueInput;
    private ColorSquareView colorSquare;
    private ColorLineView colorLine;
    private View selectedColorView;

    private boolean isUpdating = false;
    private static final float RGB_MAX = 255f;
    private static final String HEX_PREFIX = "#";

    public ColorPickerView(@NonNull Context context) {
        super(context);
        init();
    }

    public ColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        try {

            View.inflate(getContext(), R.layout.color_picker_layout, this);
            colorSquare = findViewById(R.id.color_square);
            colorLine = findViewById(R.id.color_line);
            selectedColorView = findViewById(R.id.selected_color);
            setSelectedColor(Color.RED);

            colorSquare.setOnColorSelectedListener(
                    color -> {
                        if (isUpdating) return;
                        isUpdating = true;
                        updateColorViews(color);
                        isUpdating = false;
                    });

            colorLine.setOnColorSelectedListener(
                    color -> {
                        if (isUpdating) return;
                        isUpdating = true;
                        colorSquare.setBaseColor(color);
                        updateColorViews(color);
                        isUpdating = false;
                    });
        } catch (Exception err) {
            new AlertDialog.Builder(getContext()).setMessage(err.getMessage()).create().show();
        }
    }

    private void updateColorViews(@ColorInt int color) {
        selectedColorView.setBackgroundColor(color);
        Color rgb = Color.valueOf(color);
        colorLine.setSelectedColor(color);
        colorSquare.setBaseColor(color);
    }

    @NonNull
    public static String intToHexColor(@ColorInt int colorInt) {
        return String.format("%06X", (0xFFFFFF & colorInt));
    }

    @ColorInt
    public int getSelectedColor() {
        if (selectedColorView == null
                || !(selectedColorView.getBackground() instanceof ColorDrawable)) {
            return Color.BLACK;
        }
        return ((ColorDrawable) selectedColorView.getBackground()).getColor();
    }

    public void setSelectedColor(@ColorInt int color) {
        if (isUpdating) return;
        isUpdating = true;
        updateColorViews(color);
        isUpdating = false;
    }

    public String getColorHex() {
        int color = getSelectedColor();
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public String getColorRgb() {
        int color = getSelectedColor();
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format("(%d, %d, %d)", r, g, b);
    }
}
