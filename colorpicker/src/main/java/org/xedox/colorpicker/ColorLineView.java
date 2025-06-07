package org.xedox.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorLineView extends View {
    
    private Paint paint;
    private Paint selectorPaint;
    private Bitmap colorBitmap;
    private boolean isHorizontal;
    private float selectorPosition;
    private int selectedColor = Color.RED;
    private OnColorSelectedListener colorSelectedListener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorLineView(Context context) {
        super(context);
        init();
    }

    public ColorLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(4f);
        selectorPaint.setColor(Color.WHITE);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorSelectedListener = listener;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            isHorizontal = w >= h;
            selectorPosition = isHorizontal ? w / 2f : h / 2f;
            generateColorBitmap(w, h);
            updateSelectedColor();
        }
    }

    private void generateColorBitmap(int width, int height) {
        colorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorBitmap);
        float[] hsv = new float[] {0, 1, 1};

        if (isHorizontal) {
            for (int x = 0; x < width; x++) {
                hsv[0] = 360f * x / width;
                paint.setColor(Color.HSVToColor(hsv));
                canvas.drawLine(x, 0, x, height, paint);
            }
        } else {
            for (int y = 0; y < height; y++) {
                hsv[0] = 360f * y / height;
                paint.setColor(Color.HSVToColor(hsv));
                canvas.drawLine(0, y, width, y, paint);
            }
        }
    }

    private void updateSelectedColor() {
        float hue = 360f * selectorPosition / (isHorizontal ? getWidth() : getHeight());
        float[] hsv = new float[] {hue, 1, 1};
        selectedColor = Color.HSVToColor(hsv);
        if (colorSelectedListener != null) {
            colorSelectedListener.onColorSelected(selectedColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (colorBitmap != null) {
            canvas.drawBitmap(colorBitmap, 0, 0, paint);
        }
        if (isHorizontal) {
            canvas.drawLine(selectorPosition, 0, selectorPosition, getHeight(), selectorPaint);
        } else {
            canvas.drawLine(0, selectorPosition, getWidth(), selectorPosition, selectorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            float pos = isHorizontal ? event.getX() : event.getY();
            float max = isHorizontal ? getWidth() : getHeight();
            selectorPosition = Math.max(0, Math.min(pos, max - 1));
            updateSelectedColor();
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);
        float max = isHorizontal ? getWidth() : getHeight();
        if (max > 0) {
            selectorPosition = hsv[0] * max / 360f;
            invalidate();
        }
    }
}