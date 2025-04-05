package org.xedox.webaide.console;

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
                    updateContentHeight();
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
                updateContentHeight();
                view.setY(newY);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                headerY = view.getY();
                return true;
        }
        return false;
    }

    private void updateContentHeight() {
        if (content != null && header != null) {
            int newHeight = (int) (getHeight() - header.getY() - header.getHeight());
            content.getLayoutParams().height = Math.max(0, newHeight);
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


    public void printText(String text) {
        console.printText(text);
    }

    public void printWarn(String text) {
        console.printWarn(text);
    }

    public void moveTo(float y, boolean smooth) {
        header.animate().translationY(y).start();
    }
}
