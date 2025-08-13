package org.xedox.utils.dialog;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.xedox.utils.R;

public class NeoAlertDialogBuilder extends MaterialAlertDialogBuilder {
    public NeoAlertDialogBuilder(Context context) {
        super(context, R.style.NeoAlertDialogStyle);
    }
    
    public NeoAlertDialogBuilder(Context context, int style) {
        super(context, style);
    }

}
