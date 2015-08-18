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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by andycheng on 2015/8/16.
 */
public class JsonService {

    private static final String TAG = "JsonService";

    private Geocoder geocoder;
    private Context context;
    private double current_latitude;
    private double currnet_longitude;

    public JsonService(Context c, double current_lat, double current_lon) {
        this.context = c;
        this.current_latitude = current_lat;
        this.currnet_longitude = current_lon;
    }

    public ArrayList<RealtimeItem> fromJson(String str) {

        JsonParser jsonParser = new JsonParser();
        JsonElement el = jsonParser.parse(str);
        ArrayList<RealtimeItem> items = new ArrayList<RealtimeItem>();
        int i = 0;
        JsonArray jsonArray = null;
        if(el.isJsonArray()) {
            jsonArray = el.getAsJsonArray();
            Iterator it = jsonArray.iterator();


            while(it.hasNext()) {
                JsonObject o = (JsonObject) it.next();

                RealtimeItem item = new RealtimeItem(
                          o.get("lineid").getAsString()
                        , o.get("car").getAsString()
                        , o.get("time").getAsString()
                        , o.get("location").getAsString()
                );

                items.add(item);
                i++;

                try {
                    geocoder = new Geocoder(context, new Locale("zh", "TW"));

                    String address = o.get("location").getAsString().replace("(基地台定位)","").replace("附近","");

                    //Log.d(TAG, address);
                    List<Address> addressList  = geocoder.getFromLocationName(address,1);

                    item.setLatitude(addressList.get(0).getLatitude());
                    item.setLongitude(addressList.get(0).getLongitude());
                    item.setDistance(getDistanceMeter(item.getLatitude(), item.getLongitude(), current_latitude, currnet_longitude));
                    item.setDistanceText(item.getDistance());

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        Log.d(TAG, "row #" + i);
        return items;
    }

    private double getDistanceMeter(double lat1, double lon1, double lat2, double lon2) {

        float[] results=new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];

    }
}
