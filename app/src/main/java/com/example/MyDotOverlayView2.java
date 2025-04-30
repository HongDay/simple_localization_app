package com.example;

import android.animation.ValueAnimator;
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

    private ValueAnimator animatorX;
    private ValueAnimator animatorY;

    public MyDotOverlayView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void updatePoint(float x_, float y_){
        if (!isinit) {
            x = x_;
            y = y_;
            isinit = true;
            invalidate();
            return;
        }
        if(animatorX != null && animatorX.isRunning()) animatorX.cancel();
        if (animatorY != null && animatorY.isRunning()) animatorY.cancel();

        animatorX = ValueAnimator.ofFloat(x, x_);
        animatorY = ValueAnimator.ofFloat(y, y_);

        animatorX.setDuration(300);
        animatorY.setDuration(300);

        animatorX.addUpdateListener(animation -> {
            x = (float) animation.getAnimatedValue();
            invalidate();
        });

        animatorY.addUpdateListener(animation -> {
            y = (float) animation.getAnimatedValue();
            invalidate();
        });

        animatorX.start();
        animatorY.start();

    }

    public void clearDot() {
        isinit = false;
        if (animatorX != null && animatorX.isRunning()) animatorX.cancel();
        if (animatorY != null && animatorY.isRunning()) animatorY.cancel();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isinit) canvas.drawCircle(x, y, 12, paint);
    }

}
