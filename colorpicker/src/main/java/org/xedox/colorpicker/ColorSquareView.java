package org.xedox.colorpicker;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;

public class ColorSquareView extends View {
    
    private Paint gradientPaint = new Paint();
    private Paint selectorPaint = new Paint();
    private int baseColor = Color.RED;
    private int currentColor;
    private float selectorX, selectorY;
    private OnColorChangeListener listener;
    private Bitmap colorMap;
    private float[] hsv = new float[3];

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }

    public ColorSquareView(Context context) {
        super(context);
        init();
    }

    public ColorSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gradientPaint.setAntiAlias(true);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(4f);
        selectorPaint.setColor(Color.WHITE);
        updateColor(baseColor);
    }

    public void updateColor(int color) {
        baseColor = color;
        Color.colorToHSV(baseColor, hsv);
        createColorMap();
        updateCurrentColor();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        selectorX = w / 2f;
        selectorY = h / 2f;
        createColorMap();
        updateCurrentColor();
    }

    private void createColorMap() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        colorMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorMap);
        
        LinearGradient satGradient = new LinearGradient(
                0, 0, w, 0,
                Color.HSVToColor(new float[]{hsv[0], 0, 1}),
                Color.HSVToColor(new float[]{hsv[0], 1, 1}),
                Shader.TileMode.CLAMP);
        Paint satPaint = new Paint();
        satPaint.setShader(satGradient);
        canvas.drawRect(0, 0, w, h, satPaint);

        LinearGradient valGradient = new LinearGradient(
                0, 0, 0, h,
                0x00000000,
                0xFF000000,
                Shader.TileMode.CLAMP);
        Paint valPaint = new Paint();
        valPaint.setShader(valGradient);
        canvas.drawRect(0, 0, w, h, valPaint);
    }

    private void updateCurrentColor() {
        if (colorMap != null && selectorX >= 0 && selectorY >= 0) {
            currentColor = colorMap.getPixel((int) selectorX, (int) selectorY);
            if (listener != null) listener.onColorChanged(currentColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (colorMap != null) {
            canvas.drawBitmap(colorMap, 0, 0, gradientPaint);
            canvas.drawCircle(selectorX, selectorY, 20, selectorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        selectorX = Math.max(0, Math.min(event.getX(), getWidth() - 1));
        selectorY = Math.max(0, Math.min(event.getY(), getHeight() - 1));
        updateCurrentColor();
        invalidate();
        return true;
    }

    public void setOnColorSelectedListener(OnColorChangeListener l) {
        listener = l;
    }

    public int getCurrentColor() {
        return currentColor;
    }
}