package org.xedox.utils.dialog;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import org.xedox.utils.Clipboard;
import org.xedox.utils.R;

public class ErrorDialog {
    private static final String TAG = "ErrorDialog";
    private static boolean showFullError = false;

    public static void show(Context context, Throwable err) {
        err.printStackTrace();
        DialogBuilder builder =
                new DialogBuilder(context)
                        .setTitle("Runtime error")
                        .setView(R.layout.dialog_error_layout)
                        .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                        .setCancelable(true);
        builder.setCancelable(false);

        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        CheckBox errorDetailToggle = builder.findViewById(R.id.errorDetailToggle);
        errorDetailToggle.setChecked(showFullError);
        updateErrorMessage(errorMessage, err, showFullError);
        errorMessage.setHorizontalScrollBarEnabled(true);
        errorDetailToggle.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    showFullError = isChecked;
                    updateErrorMessage(errorMessage, err, isChecked);
                });
        builder.setNegativeButton(
                "Copy",
                (d, w) -> {
                    Clipboard.copy(context, errorMessage.getText().toString());
                });
        builder.show();
    }

    private static void updateErrorMessage(
            TextView errorMessage, Throwable err, boolean fullError) {
        if (err == null) {
            errorMessage.setText("UnknownError");
            return;
        }

        if (fullError) {
            StringBuilder sb = new StringBuilder();
            Throwable current = err;
            int level = 0;

            while (current != null) {
                if (level > 0) {
                    sb.append("\n\nCaused by (").append(level).append("): ");
                }

                sb.append(current.getClass().getSimpleName())
                        .append(": ")
                        .append(current.getMessage());

                sb.append("\n\nStack trace:\n");
                for (StackTraceElement element : current.getStackTrace()) {
                    sb.append("    ").append(element.toString()).append("\n");
                }

                current = current.getCause();
                level++;
            }

            errorMessage.setText(sb.toString());
        } else {
            errorMessage.setText(err.getClass().getSimpleName() + ": " + err.getMessage());
        }
    }
}
