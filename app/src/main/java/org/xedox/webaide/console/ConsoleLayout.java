package org.xedox.webaide.console;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.xedox.webaide.R;

public class ConsoleLayout extends RelativeLayout {

    private View content;
    private LinearLayout header;
    private ConsoleView console;
    private float headerY = 0;
    private float initialTouchY;

    public ConsoleLayout(Context context) {
        super(context);
        init(context);
    }

    public ConsoleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConsoleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.console_layout, this);

        header = findViewById(R.id.header);
        content = findViewById(R.id.content);
        console = findViewById(R.id.console);

        post(
                () -> {
                    headerY = getHeight() - header.getHeight();
                    header.setY(headerY);
                    updateContent();
                    printInfo("Editor stared successful");
                });

        header.setOnTouchListener(this::handleHeaderMove);
    }

    private boolean handleHeaderMove(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchY = event.getRawY();
                headerY = view.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getRawY() - initialTouchY;
                float newY = headerY + deltaY;

                newY = Math.max(0, newY);
                newY = Math.min(getHeight() - header.getHeight(), newY);
                view.setY(newY);
                updateContent();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                headerY = view.getY();
                return true;
        }
        return false;
    }

    private void updateContent() {
        if (content != null && header != null) {
            float headerBottom = header.getY() + header.getHeight();
            content.setY(headerBottom);
            content.requestLayout();
        }
    }

    public void printError(String text) {
        console.printError(text);
    }

    public void printText(int text) {
        console.printText(getContext().getString(text));
    }

    public void printError(String text, Throwable e) {
        console.printError(text + " " + e.toString());
    }

    public void printError(int text, Throwable e) {
        console.printError(getContext().getString(text) + " " + e.toString());
    }

    public void printError(int text) {
        console.printError(getContext().getString(text));
    }
    
    public void printError(Throwable err) {
        console.printError(err.toString());
    }

    public void printText(String text) {
        console.printText(text);
    }
    
    public void printInfo(String text) {
        console.printText(text);
    }

    public void printWarn(String text) {
        console.printWarn(text);
    }

    public void moveTo(float y) {
        ObjectAnimator anim =
                ObjectAnimator.ofFloat(header, "y", header.getTranslationY(), y);
        anim.addUpdateListener(
                (valueAnimator) -> {
                    updateContent();
                });
        
        anim.start();
    }
}
