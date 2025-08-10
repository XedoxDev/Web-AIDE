package org.xedox.utils.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import org.xedox.utils.R;

public class NeoAlertDialog extends AlertDialog.Builder {
    public NeoAlertDialog(Context context) {
        super(context, R.style.NeoAlertDialogStyle);
    }
    
    public NeoAlertDialog(Context context, int style) {
        super(context, style);
    }

}
