package org.xedox.webaide.dialogs;

import android.content.Context;
import android.util.Log;
import org.xedox.webaide.R;

public class ErrorDialog {
    private static final String TAG = "ErrorDialog";

    public static void show(Context context, Throwable e) {
        show(context, getFullErrorMessage(e));
    }

    public static void show(Context context, CharSequence text, Throwable e) {
        show(context, String.format("%s:\n%s", text, getFullErrorMessage(e)));
    }

    public static void show(Context context, CharSequence text) {
        show(context, text, true);
    }

    private static void show(Context context, CharSequence text, boolean isDetailed) {
        if (context == null) {
            Log.w(TAG, "Context is null");
            return;
        }

        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle("Runtime error");
        builder.setMessage(text);

        builder.setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());
        builder.show();
    }

    private static String getFullErrorMessage(Throwable e) {
        return getFullErrorMessage(e, true);
    }

    private static String getFullErrorMessage(Throwable e, boolean fullTrace) {
        if (e == null) return "No error information available";

        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName());

        if (e.getMessage() != null) {
            sb.append(": ").append(e.getMessage());
        }

        if (fullTrace) {
            sb.append("\n\nStack Trace:\n");
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append("  at ").append(element.toString()).append("\n");
            }

            if (e.getCause() != null) {
                sb.append("\nCaused by: ").append(getFullErrorMessage(e.getCause(), true));
            }
        } else {
            sb.append("\n(").append(e.getStackTrace()[0].toString()).append(")");
        }

        return sb.toString();
    }
}
