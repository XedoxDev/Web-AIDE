package org.xedox.webaide.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import org.xedox.colorpicker.ColorPickerView;
import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.R;

public class ColorPickerDialog {
    
    private final BaseActivity context;
    private DialogBuilder builder;

    public ColorPickerDialog(BaseActivity context) {
        this.context = context;
        builder = new DialogBuilder(context);
    }

    public void show() {
        ColorPickerView colorPicker = new ColorPickerView(context);
        builder.setTitle(R.string.color_picker);
        builder.setView(colorPicker);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("RGB", (d, w) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            String rgb = colorPicker.getColorRgb();
            ClipData clip = ClipData.newPlainText("Color RGB", rgb);
            clipboard.setPrimaryClip(clip);
        });
        builder.setNeutralButton("HEX", (d, w) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            String hex = colorPicker.getColorHex();
            ClipData clip = ClipData.newPlainText("Color HEX", hex);
            clipboard.setPrimaryClip(clip);
        });
        builder.create().show();
    }

    public static void show(BaseActivity act) {
        new ColorPickerDialog(act).show();
    }
}