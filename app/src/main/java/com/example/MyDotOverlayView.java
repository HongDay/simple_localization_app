package com.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MyDotOverlayView extends View {
    private final List<PointF> points = new ArrayList<>();
    private final Paint paint = new Paint();

    public MyDotOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setDotColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public void addPoint(float x, float y) {
        points.add(new PointF(x,y));
        invalidate();
    }

    public void removeLastPoint() {
        if(!points.isEmpty()){
            points.remove(points.size() -1);
            invalidate();
        }
    }

    public void clearall() {
        points.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PointF p : points) {
            canvas.drawCircle(p.x, p.y, 12, paint);
        }
    }

}
