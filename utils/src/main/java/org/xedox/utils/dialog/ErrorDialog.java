package org.xedox.utils.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.ScrollingMovementMethod;
import android.widget.CheckBox;
import android.widget.TextView;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.xedox.utils.Clipboard;
import org.xedox.utils.R;

public class ErrorDialog {

    public static void show(Context context, Throwable error) {
        show(context, "Runtime error", buildError(error));
    }

    public static void show(Context context, String error) {
        show(context, "Runtime error", error);
    }

    public static void show(Context context, String title, String error) {
        if (error == null) return;

        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(title);
        builder.setView(R.layout.dialog_error_layout);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);

        TextView errorMessage = builder.findViewById(R.id.errorMessage);
        errorMessage.setText(error);
        errorMessage.setTypeface(Typeface.MONOSPACE);
        errorMessage.setMovementMethod(new ScrollingMovementMethod());
        errorMessage.setHorizontalScrollBarEnabled(true);
        builder.setNegativeButton(
                R.string.copy,
                (dialog, which) -> Clipboard.copy(context, errorMessage.getText().toString()));

        builder.show();
    }

    public static String buildError(Throwable throwable) {
        if (throwable == null) {
            return "NO ERROR";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        printThrowable(pw, throwable, 0);

        Throwable cause = throwable.getCause();
        int depth = 1;
        while (cause != null && cause != throwable) {
            pw.println("\nCaused by:");
            printThrowable(pw, cause, depth);
            cause = cause.getCause();
            depth++;
        }

        return sw.toString();
    }

    private static void printThrowable(PrintWriter pw, Throwable throwable, int depth) {
        String indent = "  ".repeat(depth);

        pw.println(indent + "Type: " + throwable.getClass().getSimpleName());
        pw.println(indent + "Message: " + throwable.getMessage());
        pw.println(indent + "StackTrace:");

        for (StackTraceElement element : throwable.getStackTrace()) {
            pw.println(indent + "    at " + element);
        }
    }
}
