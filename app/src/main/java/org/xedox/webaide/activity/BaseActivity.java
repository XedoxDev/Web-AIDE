package org.xedox.webaide.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
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
}
