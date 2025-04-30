package com.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.security.MessageDigest;
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

    public float[] findNearstPoint(float x, float y, float threshold){
        for (PointF point : points){
            float dx = point.x - x;
            float dy = point.y - y;
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= threshold * threshold){
                return new float[]{point.x, point.y};
            }
        }
        return null;
    }

    public void removeNearestPoint(float x, float y){
        float threshold = 1f;
        for (int i = 0; i < points.size(); i++) {
            PointF point = points.get(i);
            float dx = point.x - x;
            float dy = point.y - y;
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= threshold * threshold) {
                points.remove(i);
                invalidate();
                break;
            }
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
