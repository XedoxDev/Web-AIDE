package org.xedox.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class Clipboard {
    public static void copy(Context context, String name, String toCopy) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(name, toCopy);
        clipboard.setPrimaryClip(clipData);
    }

    public static void copy(Context context, String toCopy) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Text copyed from WebDroid", toCopy);
        clipboard.setPrimaryClip(clipData);
    }
}
