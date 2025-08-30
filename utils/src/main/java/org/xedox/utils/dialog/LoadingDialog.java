package org.xedox.utils.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.xedox.utils.R;

public class LoadingDialog {

    protected DialogBuilder builder;
    protected TextView infoText;
    protected ProgressBar progressBar;
    protected boolean isShowing = false;
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected Context context;
    protected Runnable runnable;

    public LoadingDialog(Context context, String title, Runnable run) {
        this.context = context;
        this.runnable = run;
        builder = new DialogBuilder(context);
        builder.setView(R.layout.dialog_loading_layout);
        builder.setTitle(title);
        builder.setCancelable(false);

        infoText = builder.findViewById(R.id.loading_info);
        progressBar = builder.findViewById(R.id.progress);

        if (runnable != null) {
            startBackgroundTask();
        }
    }

    public LoadingDialog(Context context, int title, Runnable run) {
        this(context, context.getString(title), run);
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
        if (isShowing && runnable != null) {
            startBackgroundTask();
        }
    }

    private void startBackgroundTask() {
        runnable.run();
        dismiss();
    }

    public void show() {
        handler.post(
                () -> {
                    if (!isShowing) {
                        builder.show();
                        isShowing = true;
                        if (runnable != null) {
                            startBackgroundTask();
                        }
                    }
                });
    }

    public void dismiss() {
        handler.post(
                () -> {
                    if (isShowing) {
                        builder.dialog.dismiss();
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

    public Handler getHandler() {
        return handler;
    }

    public static LoadingDialog create(Context context, String title, Runnable run) {
        return new LoadingDialog(context, title, run);
    }

    public static LoadingDialog create(Context context, int title, Runnable run) {
        return create(context, context.getString(title), run);
    }
}
