package org.xedox.webaide.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.xedox.webaide.IDE;
import org.xedox.webaide.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String TAG = "BaseActivity";
    protected Toolbar toolbar;

    public View root;
    
    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    onFileSelected(uri);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = findViewById(android.R.id.content);
        IDE.init(this);
    }

    protected void loadToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
    }

    public void showSnackbar(int message) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok, (v) -> {})
                .show();
    }

    public void showSnackbar(String message) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok, (v) -> {})
                .show();
    }

    public void showSnackbar(Throwable message) {
        Snackbar.make(root, message.getLocalizedMessage(), Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok, (v) -> {})
                .show();
    }

    public void showDialog(String message) {
        new MaterialAlertDialogBuilder(this).setMessage(message).create().show();
    }

    protected void setToolbarTitle(String newTitle) {
        if (toolbar.getTitle() != null && toolbar.getTitle().equals(newTitle)) return;
        View titleView = toolbar.getChildAt(0);
        if (titleView == null) {
            toolbar.setTitle(newTitle);
            return;
        }
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(titleView, "alpha", 1f, 0f);
        ObjectAnimator slideOut =
                ObjectAnimator.ofFloat(titleView, "translationY", 0f, titleView.getHeight() / 2f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(titleView, "alpha", 0f, 1f);
        ObjectAnimator slideIn =
                ObjectAnimator.ofFloat(titleView, "translationY", -titleView.getHeight() / 2f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(fadeOut).with(slideOut);
        animatorSet.play(fadeIn).with(slideIn).after(fadeOut);
        fadeOut.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolbar.setTitle(newTitle);
                    }
                });
        animatorSet.setDuration(300).start();
    }
    
    public void setSubtitle(String title) {
        getSupportActionBar().setSubtitle(title);
    }
    
    public void showFilePicker(SelectListener listener) {
        filePickerLauncher.launch(new String[]{"*/*"});
        mSelectListener = listener;
    }
    
    protected void onFileSelected(Uri uri) {
        if (mSelectListener != null) {
            try {
                String fileName = getFileName(uri);
                String fileContent = readFileContent(uri);
                mSelectListener.onSelect(uri, fileName, fileContent);
            } catch (Exception e) {
                mSelectListener.onSelect();
                showSnackbar("Error reading file: " + e.getMessage());
            }
        }
    }
    
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    private String readFileContent(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }
    
    private SelectListener mSelectListener;
    
    public interface SelectListener {
        void onSelect(Object... options); // file picker options: [0]=Uri, [1]=filename, [2]=content (null if cancelled)
    }
}