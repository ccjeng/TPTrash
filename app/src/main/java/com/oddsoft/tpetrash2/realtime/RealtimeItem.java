package com.oddsoft.tpetrash2.realtime;

import java.text.DecimalFormat;


/**
 * Created by andycheng on 2015/8/11.
 */
public class RealtimeItem {
    private static final String TAG = "RealtimeItem";

    private String lineid;
    private String car;
    private String time;
    private String location;
    private double latitude;
    private double longitude;
    private double distance;
    private String distanceText;

    public RealtimeItem(String lineid, String car, String time, String location) {
        this.lineid = lineid;
        this.car = car;
        this.time = time;
        this.location = location;
    }

    public String getLineid() {
        return lineid;
    }

    public String getCarNO() {
        return car;
    }

    public String getCarTime() {
        return time;
    }

    public String getCarLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double v) {
        latitude = v;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double v) {
        longitude = v;
    }

    public void setDistance(double v) {
        DecimalFormat df = new DecimalFormat("#");
        distance = Double.parseDouble(df.format(v));

        if (latitude!=0) {}
        else {
            distance = 999999999;
        }
    }

    public double getDistance() {
        //if (distance == 0)  {
        //    distance = 999999999;
        //}
        return distance;
    }

    public void setDistanceText(double v) {
        distanceText = distanceText(v);
    }

    public String getDistanceText() {
        if (distanceText == null) {
            distanceText = "無法定位";
        }
        return distanceText;
    }

    private String distanceText(double distance) {
        String result = "";

        if(distance < 1000 ) {
            result = String.valueOf((int) distance) + "公尺";
        }
        else {
            result = new DecimalFormat("#").format(distance / 1000) + "公里";
        }

        if (latitude==0) {
            result = "無法定位";
        }

        return result;
    }

}
