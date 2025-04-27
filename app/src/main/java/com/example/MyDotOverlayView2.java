package com.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class MyDotOverlayView2 extends View {
    private final Paint paint = new Paint();

    private float x;
    private float y;
    private boolean isinit = false;

    public MyDotOverlayView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void updatePoint(float x_, float y_){
        x = x_;
        y = y_;
        isinit = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isinit) canvas.drawCircle(x, y, 12, paint);
    }

}
