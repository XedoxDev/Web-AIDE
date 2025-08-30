package org.xedox.webaide.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import org.xedox.utils.Assets;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.utils.dialog.LoadingDialog;
import org.xedox.webaide.AppCore;
import org.xedox.webaide.R;
import org.xedox.webaide.sora.SoraEditorManager;

public class CopyAssetsDialog extends LoadingDialog {

    private Context mContext;

    private CopyAssetsDialog(Context context) {
        super(context, R.string.copying_assets, null);
        this.mContext = context;
        setRunnable(this::copyAssets);
    }

    public static void show(Context context) {
        CopyAssetsDialog d = new CopyAssetsDialog(context);
        d.setMaxProgress(-1);
        d.show();
    }

    private void copyAssets() {
        Assets ass = Assets.from(mContext);
        try {
            ass.copyAssetsRecursive("textmate", AppCore.file("textmate"));
            getHandler().post(() -> SoraEditorManager.initialize(mContext));
        } catch (Exception err) {
            getHandler().post(() -> ErrorDialog.show(mContext, err));
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putBoolean("isCopyedAssets", true).apply();
        dismiss();
    }
}