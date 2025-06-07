package org.xedox.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSquareView extends View {

    private Paint paint, selector;
    private float[] hsv = new float[] {0f, 1f, 1f};
    private int baseColor = Color.RED;
    private Bitmap bitmap;
    private int selectedColor;
    private float touchX = 0, touchY = 0;
    private OnColorSelectedListener colorSelectedListener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorSquareView(Context context) {
        super(context);
        init();
    }

    public ColorSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorSquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        selector = new Paint(Paint.ANTI_ALIAS_FLAG);
        selector.setStyle(Paint.Style.STROKE);
        selector.setStrokeWidth(4f);
        selector.setColor(Color.WHITE);
    }

    public void setBaseColor(int color) {
        this.baseColor = color;
        Color.colorToHSV(baseColor, hsv);
        if (getWidth() > 0 && getHeight() > 0) {
            createBitmap();
        }
        invalidate();
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorSelectedListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            createBitmap();
            touchX = w / 2f;
            touchY = h / 2f;
        }
    }

    private void createBitmap() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawColorSquare(canvas, getWidth(), getHeight());
        updateSelectedColor();
    }

    private void drawColorSquare(Canvas canvas, int width, int height) {
        final int STEP = 4;
        for (int x = 0; x < width; x += STEP) {
            for (int y = 0; y < height; y += STEP) {
                float saturation = (float) x / width;
                float value = 1f - (float) y / height;
                int color = Color.HSVToColor(new float[] {hsv[0], saturation, value});
                paint.setColor(color);
                canvas.drawRect(x, y, x + STEP, y + STEP, paint);
            }
        }
    }

    private void updateSelectedColor() {
        if (bitmap != null && touchX >= 0 && touchY >= 0 
                && touchX < getWidth() && touchY < getHeight()) {
            selectedColor = bitmap.getPixel((int) touchX, (int) touchY);
            if (colorSelectedListener != null) {
                colorSelectedListener.onColorSelected(selectedColor);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
            canvas.drawCircle(touchX, touchY, 20, selector);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touchX = Math.max(0, Math.min(event.getX(), getWidth() - 1));
                touchY = Math.max(0, Math.min(event.getY(), getHeight() - 1));
                updateSelectedColor();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }
}