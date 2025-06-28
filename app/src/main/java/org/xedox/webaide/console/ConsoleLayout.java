package org.xedox.webaide.console;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xedox.webaide.R;

public class ConsoleLayout extends RelativeLayout {

    private View content;
    private LinearLayout header;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ConsoleAdapter adapter;
    private float headerY = 0;
    private float initialTouchY;
    public static String appLogsName;
    public static String buildLogsName;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, List<String>> messageQueue = new HashMap<>();

    public ConsoleLayout(Context context) {
        this(context, null);
    }

    public ConsoleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsoleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        appLogsName = context.getString(R.string.app_logs);
        buildLogsName = context.getString(R.string.build_logs);
        init(context);
    }

    private void init(Context context) {
        if (!(context instanceof FragmentActivity)) {
            throw new IllegalArgumentException("Context must be a FragmentActivity");
        }

        inflate(context, R.layout.console_layout, this);
        header = findViewById(R.id.header);
        content = findViewById(R.id.content);
        viewPager = findViewById(R.id.tabs_content);
        tabLayout = findViewById(R.id.tab_layout);
        adapter = new ConsoleAdapter((FragmentActivity) context);
        adapter.addFragment(ConsoleFragment.newInstance(appLogsName));
        adapter.addFragment(ConsoleFragment.newInstance(buildLogsName));
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(
                        tabLayout,
                        viewPager,
                        (tab, position) -> tab.setText(adapter.get(position).getName()))
                .attach();

        post(
                () -> {
                    headerY = getHeight() - header.getHeight();
                    header.setY(headerY);
                    updateContent();
                    postDelayed(() -> printText(appLogsName, "IDE Editor start successful"), 500);
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
                float newY =
                        Math.max(0, Math.min(getHeight() - header.getHeight(), headerY + deltaY));
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

    public ConsoleFragment getCurrentConsole() {
        return adapter.get(tabLayout.getSelectedTabPosition());
    }

    private ConsoleFragment getConsoleByName(String name) {
        return adapter.getByName(name);
    }

    public void printText(String fragmentName, String text) {
        handler.post(
                () -> {
                    ConsoleFragment fragment = getConsoleByName(fragmentName);
                    if (fragment != null && fragment.isAdded() && fragment.console != null) {
                        fragment.console.printText(text);
                        processQueuedMessages(fragmentName);
                    } else {
                        queueMessage(fragmentName, text);
                    }
                });
    }

    private void queueMessage(String fragmentName, String text) {
        if (!messageQueue.containsKey(fragmentName)) {
            messageQueue.put(fragmentName, new ArrayList<>());
        }
        messageQueue.get(fragmentName).add(text);
    }

    private void processQueuedMessages(String fragmentName) {
        if (messageQueue.containsKey(fragmentName)) {
            ConsoleFragment fragment = getConsoleByName(fragmentName);
            if (fragment != null && fragment.isAdded() && fragment.console != null) {
                for (String message : messageQueue.get(fragmentName)) {
                    fragment.console.printText(message);
                }
                messageQueue.remove(fragmentName);
            }
        }
    }

    public void printError(String fragmentName, String text) {
        handler.post(
                () -> {
                    ConsoleFragment fragment = getConsoleByName(fragmentName);
                    if (fragment != null && fragment.isAdded() && fragment.console != null) {
                        fragment.console.printError(text);
                        processQueuedMessages(fragmentName);
                    } else {
                        queueMessage(fragmentName, "[ERROR] " + text);
                    }
                });
    }

    public void printWarn(String fragmentName, String text) {
        handler.post(
                () -> {
                    ConsoleFragment fragment = getConsoleByName(fragmentName);
                    if (fragment != null && fragment.isAdded() && fragment.console != null) {
                        fragment.console.printWarn(text);
                        processQueuedMessages(fragmentName);
                    } else {
                        queueMessage(fragmentName, "[WARN] " + text);
                    }
                });
    }

    public void printError(String text) {
        printError(appLogsName, text);
    }

    public void printText(String text) {
        printText(appLogsName, text);
    }

    public void printWarn(String text) {
        printWarn(appLogsName, text);
    }

    public void printError(Throwable err) {
        printError(appLogsName, err.toString());
    }

    public void printText(int textRes) {
        printText(appLogsName, getContext().getString(textRes));
    }

    public void printError(int textRes) {
        printError(appLogsName, getContext().getString(textRes));
    }

    public void printError(int textRes, Throwable e) {
        printError(appLogsName, getContext().getString(textRes) + " " + e.toString());
    }

    public void printError(String fragmentName, Throwable err) {
        printError(fragmentName, err.toString());
    }

    public void printText(String fragmentName, int textRes) {
        printText(fragmentName, getContext().getString(textRes));
    }

    public void printError(String fragmentName, int textRes) {
        printError(fragmentName, getContext().getString(textRes));
    }

    public void printError(String fragmentName, int textRes, Throwable e) {
        printError(fragmentName, getContext().getString(textRes) + " " + e.toString());
    }

    public void printError(String fragmentName, String text, Throwable e) {
        printError(fragmentName, text + " " + e.toString());
    }

    public void println(String text) {
        printText(text);
    }

    public void printf(String format, Object... args) {
        printText(String.format(format, args));
    }

    public void println(String fragmentName, String text) {
        printText(fragmentName, text);
    }

    public void printf(String fragmentName, String format, Object... args) {
        printText(fragmentName, String.format(format, args));
    }

    public void printStackTrace(Throwable e) {
        printError(e.toString());
        for (StackTraceElement element : e.getStackTrace()) {
            printError("    at " + element.toString());
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            printError("Caused by: " + cause.toString());
            printStackTrace(cause);
        }
    }

    public void printStackTrace(String fragmentName, Throwable e) {
        printError(fragmentName, e.toString());
        for (StackTraceElement element : e.getStackTrace()) {
            printError(fragmentName, "    at " + element.toString());
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            printError(fragmentName, "Caused by: " + cause.toString());
            printStackTrace(fragmentName, cause);
        }
    }

    public void clearCurrent() {
        ConsoleFragment fragment = getCurrentConsole();
        if (fragment != null && fragment.console != null) {
            fragment.console.clear();
        }
    }

    public void clear(String fragmentName) {
        ConsoleFragment fragment = getConsoleByName(fragmentName);
        if (fragment != null && fragment.console != null) {
            fragment.console.clear();
        }
    }

    public void clearAll() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ConsoleFragment fragment = adapter.get(i);
            if (fragment != null && fragment.console != null) {
                fragment.console.clear();
            }
        }
    }

    private int time = 300;

    public void moveTo(float y) {
        headerY = header.getY();
        ValueAnimator anim = ValueAnimator.ofFloat(headerY, y);
        anim.setDuration(time);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(
                animator -> {
                    float currentValue = (float) animator.getAnimatedValue();
                    header.setY(currentValue);
                    headerY = currentValue;
                    updateContent();
                });
        anim.start();
    }

    public OutputStream getStreamBuild() {
        return getConsoleByName(buildLogsName).console.getOutputStream();
    }
}
