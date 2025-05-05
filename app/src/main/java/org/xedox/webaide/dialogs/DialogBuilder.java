package org.xedox.webaide.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogBuilder {
    public final MaterialAlertDialogBuilder builder;
    public AlertDialog dialog;
    private OnButtonClickListener positiveListener;
    private OnButtonClickListener negativeListener;
    private OnButtonClickListener neutralListener;
    private View customView;

    public static final boolean EXIT = true;
    public static final boolean RETURN = false;

    public DialogBuilder(Context context) {
        this.builder = new MaterialAlertDialogBuilder(context);
    }

    public DialogBuilder setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }

    public DialogBuilder setTitle(int titleResId) {
        builder.setTitle(titleResId);
        return this;
    }

    public DialogBuilder setMessage(CharSequence message) {
        builder.setMessage(message);
        return this;
    }

    public DialogBuilder setMessage(int messageResId) {
        builder.setMessage(messageResId);
        return this;
    }

    public DialogBuilder setView(View view) {
        this.customView = view;
        builder.setView(view);
        return this;
    }

    public DialogBuilder setView(int layoutResId) {
        View view = LayoutInflater.from(builder.getContext()).inflate(layoutResId, null);
        return setView(view);
    }

    public DialogBuilder setPositiveButton(CharSequence text, OnButtonClickListener listener) {
        builder.setPositiveButton(text, null);
        this.positiveListener = listener;
        return this;
    }

    public DialogBuilder setPositiveButton(int textResId, OnButtonClickListener listener) {
        return setPositiveButton(builder.getContext().getString(textResId), listener);
    }

    public DialogBuilder setNegativeButton(CharSequence text, OnButtonClickListener listener) {
        builder.setNegativeButton(text, null);
        this.negativeListener = listener;
        return this;
    }

    public DialogBuilder setNegativeButton(int textResId, OnButtonClickListener listener) {
        return setNegativeButton(builder.getContext().getString(textResId), listener);
    }

    public DialogBuilder setNeutralButton(CharSequence text, OnButtonClickListener listener) {
        builder.setNeutralButton(text, null);
        this.neutralListener = listener;
        return this;
    }

    public DialogBuilder setNeutralButton(int textResId, OnButtonClickListener listener) {
        return setNeutralButton(builder.getContext().getString(textResId), listener);
    }

    private void setupButton(int whichButton, OnButtonClickListener listener) {
        if (listener != null && dialog != null) {
            dialog.getButton(whichButton)
                    .setOnClickListener(
                            view -> {
                                if (listener.onClick(dialog, whichButton) == EXIT) {
                                    dialog.dismiss();
                                }
                            });
        }
    }

    public AlertDialog create() {
        dialog = builder.create();
        dialog.setOnShowListener(
                dialogInterface -> {
                    setupButton(AlertDialog.BUTTON_POSITIVE, positiveListener);
                    setupButton(AlertDialog.BUTTON_NEGATIVE, negativeListener);
                    setupButton(AlertDialog.BUTTON_NEUTRAL, neutralListener);
                });
        return dialog;
    }

    public void show() {
        create().show();
    }

    public View getView() {
        return customView;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        return customView != null ? (T) customView.findViewById(id) : null;
    }

    public interface OnButtonClickListener {
        boolean onClick(AlertDialog dialog, int which);
    }

    public DialogBuilder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        builder.setItems(items, listener);
        return this;
    }
}
