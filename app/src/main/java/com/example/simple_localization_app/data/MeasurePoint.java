package com.example.simple_localization_app.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeasurePoint {
    public float x;
    public float y;
    public List<InfoRssi> apList;

    public MeasurePoint(float x, float y) {
        this.x = x;
        this.y = y;
        this.apList = new ArrayList<>();
    }

    public void addRssi(InfoRssi records){
        apList.add(records);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MeasurePoint(x=").append(x)
                .append(", y=").append(y)
                .append(", records=").append(apList.size()).append(")\n");

        for (InfoRssi info : apList) {
            sb.append("  Time: ").append(info.timestamp).append("\n");
            for (Map.Entry<String, Integer> entry : info.apInfoMap.entrySet()) {
                sb.append("    ").append(entry.getKey())
                        .append(" => ").append(entry.getValue()).append(" dBm\n");
            }
        }

        return sb.toString();
    }

}
