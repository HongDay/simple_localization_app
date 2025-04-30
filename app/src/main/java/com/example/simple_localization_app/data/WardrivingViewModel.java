package com.example.simple_localization_app.data;

import androidx.lifecycle.ViewModel;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class WardrivingViewModel extends ViewModel {
    private final List<MeasurePoint> pointList = new ArrayList<>();

    public void addMeasurePoint(MeasurePoint point){
        pointList.add(point);
    }

    public MeasurePoint getLastPoint() {
        if(!pointList.isEmpty()){
            return pointList.get(pointList.size()-1);
        }
        return null;
    }

    public void removeLastPoint() {
        if(!pointList.isEmpty()){
            pointList.remove(pointList.size()-1);
        }
    }

    public void removeCoordinatePoint(float x, float y) {
        float threshold = 1f;
        for (int i = 0; i < pointList.size(); i++){
            MeasurePoint point = pointList.get(i);
            if (Math.abs(point.x -x) < threshold && Math.abs(point.y - y) < threshold){
                pointList.remove(i);
                break;
            }
        }
    }

    public MeasurePoint findMeasurePointByCoordinates(float x, float y) {
        float threshold = 1f;
        for (MeasurePoint point : getAllPoints()) {
            if (Math.abs(point.x - x) < threshold && Math.abs(point.y - y) < threshold) {
                return point;
            }
        }
        return null;
    }


    public void reset(){
        pointList.clear();
    }

    public List<MeasurePoint> getAllPoints() {
        return pointList;
    }
}
