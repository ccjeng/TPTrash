package com.oddsoft.tpetrash2.realtime;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;

@ParseClassName("RealTime")
public class RealtimeOItem extends ParseObject {

    public String getAddress() {
        return getString("address");
    }

    public String getCarTime() {
        return getString("cartime");
    }

    public String getCarNo() {
        return getString("carno");
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public String getDistance(ParseGeoPoint current) {

        Double distance = getLocation().distanceInKilometersTo(current);
        String strDistance = "";
        DecimalFormat df;
        String unit = "";

        if (distance < 1) {
            distance = distance * 1000;
            unit = " 公尺";
        } else {
            unit = " 公里";
        }
        df = new DecimalFormat("#");
        strDistance = df.format(distance);

        return strDistance + unit;
    }

    public static ParseQuery<RealtimeOItem> getQuery() {
        return ParseQuery.getQuery(RealtimeOItem.class);
    }
}
