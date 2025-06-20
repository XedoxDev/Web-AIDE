package org.xedox.webaide.util;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import org.xedox.webaide.R;

public class AlertDialogBuilderX extends AlertDialog.Builder {
    public AlertDialogBuilderX(Context context) {
        super(context, R.style.AlertDialogXTheme);
        init();
    }
    
    public AlertDialogBuilderX(Context context, int style) {
        super(context, style);
        init();
    }

    private void init() {
    }
}
