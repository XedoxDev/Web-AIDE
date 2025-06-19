package org.xedox.webaide.console;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.xedox.webaide.R;

public class ConsoleLayout extends RelativeLayout {

    private View content;
    private LinearLayout header;
    private ConsoleView console;
    private float headerY = 0;
    private float initialTouchY;
    private GestureDetector gestureDetector;

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
        gestureDetector = new GestureDetector(context, new GestureListener());
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
        if (gestureDetector.onTouchEvent(event)) return true;
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

    private void gesture() {}

    public void minimizeConsole() {
        moveTo(getHeight() - header.getHeight());
    }

    public void centerConsole() {
        moveTo(getHeight() / 2 - header.getHeight());
    }

    public void maxConsole() {
        moveTo(0);
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
        ObjectAnimator anim = ObjectAnimator.ofFloat(header, "y", header.getTranslationY(), y);
        anim.addUpdateListener(
                (valueAnimator) -> {
                    updateContent();
                });

        anim.start();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (headerY > getHeight() / 1.7) {
                centerConsole();
                return true;
            } else if (headerY > getHeight() / 3.0) {
                maxConsole();
                return true;
            } else {
                minimizeConsole();
                return true;
            }
        }
    }
}
