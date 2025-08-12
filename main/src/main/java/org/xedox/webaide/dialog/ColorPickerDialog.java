package org.xedox.webaide.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import org.xedox.colorpicker.ColorPickerView;
import org.xedox.utils.BaseActivity;
import org.xedox.utils.Clipboard;
import org.xedox.utils.dialog.DialogBuilder;
import org.xedox.webaide.R;

public class ColorPickerDialog {

    public static void show(Context context) {
        DialogBuilder builder = new DialogBuilder(context);
        ColorPickerView colorPicker = new ColorPickerView(context);
        builder.setTitle(R.string.color_picker);
        builder.setView(colorPicker);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                "RGB",
                (d, w) -> {
                    String rgb = colorPicker.getColorRgb();
                    Clipboard.copy(context, "RGB color", rgb);
                });
        builder.setNeutralButton(
                "HEX",
                (d, w) -> {
                    String hex = colorPicker.getColorHex();
                    Clipboard.copy(context, "HEX color", hex);
                });
        builder.show();
    }
}
