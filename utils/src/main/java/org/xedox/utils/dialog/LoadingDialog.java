package org.xedox.utils.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.xedox.utils.R;

public class LoadingDialog {

    private DialogBuilder dialog;
    private TextView infoText;
    private ProgressBar progressBar;
    private boolean isShowing = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    public LoadingDialog(Context context, String title, Runnable run) {
        dialog = new DialogBuilder(context);
        dialog.setView(R.layout.dialog_loading_layout);
        dialog.setTitle(title);
        dialog.setCancelable(false);

        infoText = dialog.findViewById(R.id.loading_info);
        progressBar = dialog.findViewById(R.id.progress);
        new Thread(run).start();
    }

    public void show() {
        handler.post(
                () -> {
                    if (!isShowing) {
                        dialog.show();
                        isShowing = true;
                    }
                });
    }

    public void dismiss() {
        handler.post(
                () -> {
                    if (isShowing) {
                        dialog.dialog.dismiss();
                        isShowing = false;
                    }
                });
    }

    public void setMaxProgress(int max) {
        handler.post(
                () -> {
                    if (max <= 0) {
                        progressBar.setIndeterminate(true);
                    } else {
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(max);
                    }
                });
    }

    public void updateProgress(String message, int progress) {
        handler.post(
                () -> {
                    if (isShowing) {
                        if (message != null) infoText.setText(message);
                        if (!progressBar.isIndeterminate()) {
                            progressBar.setProgress(progress);
                        }
                    }
                });
    }

    public static LoadingDialog create(Context context, String title,  Runnable run) {
        return new LoadingDialog(context, title, run);
    }

    public static LoadingDialog create(Context context, int title,  Runnable run) {
        return create(context, context.getString(title), run);
    }
}
