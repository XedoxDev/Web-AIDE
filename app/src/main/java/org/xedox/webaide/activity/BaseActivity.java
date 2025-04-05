package org.xedox.webaide.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import org.xedox.webaide.IDE;
import org.xedox.webaide.R;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String TAG = "BaseActivity";

    public View root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = findViewById(android.R.id.content);
        IDE.init(this);
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
}
