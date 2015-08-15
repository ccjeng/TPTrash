package com.oddsoft.tpetrash2.realtime;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
    private double current_latitude;
    private double currnet_longitude;
    private double distance;
    private String distanceText;


    private Context context;
    private Geocoder geocoder;

    public RealtimeItem() {
        //this.context = c;
    }

    public RealtimeItem(Context c, String lineid, String car, String time, String location, double current_lat, double current_lon) {
        this.context = c;
        this.lineid = lineid;
        this.car = car;
        this.time = time;
        this.location = location;
        this.current_latitude = current_lat;
        this.currnet_longitude = current_lon;
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
       try {
           geocoder = new Geocoder(context, new Locale("zh", "TW"));

           //Log.d(TAG, location + "--" + geocoder.toString());

           //Log.d(TAG, Double.toString(current_latitude) + "-"+ Double.toString(currnet_longitude));
           List<Address> addressList  = geocoder.getFromLocationName(location,1);

           this.setLatitude(addressList.get(0).getLatitude());
           this.setLongitude(addressList.get(0).getLongitude());
           this.setDistance(getDistanceMeter(latitude, longitude, current_latitude, currnet_longitude));
           this.setDistanceText(distance);

       } catch (Exception e) {
           Log.d(TAG, e.toString());
        }

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
        if (distance == 0)  {
            distance = 999999999;
        }
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
    public ArrayList<RealtimeItem> fromJson(String str, Context c
            , double current_lat, double current_lon) {

        JsonParser jsonParser = new JsonParser();
        JsonElement el = jsonParser.parse(str);
        ArrayList<RealtimeItem> items = new ArrayList<RealtimeItem>();

        JsonArray jsonArray = null;
        if(el.isJsonArray()) {
            jsonArray = el.getAsJsonArray();
            Iterator it = jsonArray.iterator();
            while(it.hasNext()) {
                JsonObject o = (JsonObject) it.next();

                RealtimeItem item = new RealtimeItem( c
                        , o.get("lineid").getAsString()
                        , o.get("car").getAsString()
                        , o.get("time").getAsString()
                        , o.get("location").getAsString()
                        , current_lat
                        , current_lon
                );

                items.add(item);
            }
        }

        return items;
    }

    private double getDistanceMeter(double lat1, double lon1, double lat2, double lon2) {

        float[] results=new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];

    }

    private String distanceText(double distance)
    {
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
