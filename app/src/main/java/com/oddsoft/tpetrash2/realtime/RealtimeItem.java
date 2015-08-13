package com.oddsoft.tpetrash2.realtime;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
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

    private Context context;
    private Geocoder geocoder;

    public RealtimeItem() {
        //this.context = c;
    }

    public RealtimeItem(Context c, String lineid, String car, String time, String location) {
        this.context = c;
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
       try {

           geocoder = new Geocoder(context, new Locale("zh", "TW"));

           Log.d(TAG, location + "--" + geocoder.toString());

           List<Address> addressList  = geocoder.getFromLocationName(location,1);

           //String s = addressList.get(0).toString();

           this.setLatitude(addressList.get(0).getLatitude());
           this.setLongitude(addressList.get(0).getLongitude());

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

    public ArrayList<RealtimeItem> fromJson(String str, Context c) {

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
                );

                items.add(item);

            }
        }


        return items;
    }

    private List<Address> getGeoCode(String location, Context context) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(
                    location, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addressList;
    }

}
