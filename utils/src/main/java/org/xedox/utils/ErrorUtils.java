package org.xedox.utils;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;
import org.xedox.utils.Clipboard;
import org.xedox.utils.R;

public class ErrorUtils {
    private static final int MAX_STACK_TRACE_LINES = 50;
    private static final int MAX_CAUSE_LEVELS = 5;

    public static void updateErrorMessage(TextView errorMessage, Throwable error, boolean fullError) {
        if (error == null) {
            errorMessage.setText("Unknown error occurred");
            return;
        }

        String message = fullError ? buildFullErrorMessage(error) : buildSimpleErrorMessage(error);
        errorMessage.setText(message);
    }

    public static void copyErrorToClipboard(Context context, String errorText) {
        Clipboard.copy(context, errorText);
        Toast.makeText(context, "Error copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public static String buildSimpleErrorMessage(Throwable error) {
        return error.getClass().getSimpleName() + ": " + 
               (error.getMessage() != null ? error.getMessage() : "No error message");
    }

    public static String buildFullErrorMessage(Throwable error) {
        StringBuilder sb = new StringBuilder();
        Throwable current = error;
        int level = 0;

        while (current != null && level < MAX_CAUSE_LEVELS) {
            if (level > 0) {
                sb.append("\n\n╔═══════════════════════════════════════════════════════════╗");
                sb.append("\n║ CAUSED BY (").append(level).append("): ");
                sb.append("\n╚═══════════════════════════════════════════════════════════╝\n");
            }

            sb.append("▌ Exception: ").append(current.getClass().getName()).append("\n");
            sb.append("▌ Message: ").append(current.getMessage() != null ? 
                current.getMessage() : "No message").append("\n");
            
            sb.append("\n▌ Stack Trace:\n");
            appendStackTrace(sb, current.getStackTrace());

            current = current.getCause();
            level++;
        }

        if (level >= MAX_CAUSE_LEVELS && current != null) {
            sb.append("\n\n... and ").append(countRemainingCauses(current))
              .append(" more causes (truncated)");
        }

        return sb.toString();
    }

    private static void appendStackTrace(StringBuilder sb, StackTraceElement[] stackTrace) {
        int linesToShow = Math.min(stackTrace.length, MAX_STACK_TRACE_LINES);
        
        for (int i = 0; i < linesToShow; i++) {
            StackTraceElement element = stackTrace[i];
            sb.append("    at ").append(element.toString()).append("\n");
        }
        
        if (stackTrace.length > MAX_STACK_TRACE_LINES) {
            sb.append("    ... and ").append(stackTrace.length - MAX_STACK_TRACE_LINES)
              .append(" more lines\n");
        }
    }

    private static int countRemainingCauses(Throwable error) {
        int count = 0;
        Throwable current = error;
        while (current != null) {
            count++;
            current = current.getCause();
        }
        return count;
    }

    public static void logErrorForReporting(Throwable error) {
        android.util.Log.e("ErrorReport", "Error details:", error);
    }
}