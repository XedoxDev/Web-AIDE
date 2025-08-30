package org.xedox.utils.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogBuilder {
    public static Class<? extends AlertDialog.Builder> builderType =
            MaterialAlertDialogBuilder.class;
    public AlertDialog.Builder builder;
    private OnButtonClickListener positiveListener;
    private OnButtonClickListener negativeListener;
    private OnButtonClickListener neutralListener;
    private View customView;
    public AlertDialog dialog;

    public DialogBuilder(Context context) {
        try {
            this.builder = builderType.getConstructor(Context.class).newInstance(context);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
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
                    .setOnClickListener(view -> listener.onClick(dialog, whichButton));
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
        void onClick(AlertDialog dialog, int which);
    }

    public DialogBuilder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        builder.setItems(items, listener);
        return this;
    }

    public DialogBuilder setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }

    public DialogBuilder setIcon(int iconResId) {
        builder.setIcon(iconResId);
        return this;
    }

    public DialogBuilder setIconAttribute(int attrId) {
        builder.setIconAttribute(attrId);
        return this;
    }

    public DialogBuilder setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        return this;
    }

    public DialogBuilder setOnDismissListener(DialogInterface.OnDismissListener listener) {
        builder.setOnDismissListener(listener);
        return this;
    }

    public DialogBuilder setOnKeyListener(DialogInterface.OnKeyListener listener) {
        builder.setOnKeyListener(listener);
        return this;
    }

    public DialogBuilder setSingleChoiceItems(
            CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
        builder.setSingleChoiceItems(items, checkedItem, listener);
        return this;
    }

    public DialogBuilder setSingleChoiceItems(
            int itemsId, int checkedItem, DialogInterface.OnClickListener listener) {
        builder.setSingleChoiceItems(itemsId, checkedItem, listener);
        return this;
    }

    public DialogBuilder setMultiChoiceItems(
            CharSequence[] items,
            boolean[] checkedItems,
            DialogInterface.OnMultiChoiceClickListener listener) {
        builder.setMultiChoiceItems(items, checkedItems, listener);
        return this;
    }

    public DialogBuilder setMultiChoiceItems(
            int itemsId,
            boolean[] checkedItems,
            DialogInterface.OnMultiChoiceClickListener listener) {
        builder.setMultiChoiceItems(itemsId, checkedItems, listener);
        return this;
    }

    public DialogBuilder setCustomTitle(View customTitleView) {
        builder.setCustomTitle(customTitleView);
        return this;
    }
}
