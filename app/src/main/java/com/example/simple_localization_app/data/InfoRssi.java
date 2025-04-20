package com.example.simple_localization_app.data;

import java.util.HashMap;
import java.util.Map;

public class InfoRssi {
    public long timestamp;
    public Map<String, Integer> apInfoMap;

    public InfoRssi(long timestamp){
        this.timestamp = timestamp;
        this.apInfoMap = new HashMap<>();
    }

    public void addApInfo(String bssid, int rssi) {
        apInfoMap.put(bssid, rssi);
    }
}
