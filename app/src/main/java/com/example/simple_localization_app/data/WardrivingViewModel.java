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

    public void reset(){
        pointList.clear();
    }

    public List<MeasurePoint> getAllPoints() {
        return pointList;
    }
}
