package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oddsoft.tpetrash2.realtime.RealtimeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by andycheng on 2015/8/11.
 */
public class NewTaipeiRealtimeActivity extends Activity {

    private static final String TAG = "NewTaipeiRealtime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtaipeirealtime);

        getData();
    }

    private void getData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://data.ntpc.gov.tw/od/data/api/28AB4122-60E1-4065-98E5-ABCCB69AACA6?$format=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        readJSON(response);
                        //Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });

        queue.add(stringRequest);
    }


    private void readJSON(String str)
    {

        //Gson gson = new Gson();

        RealtimeItem item = new RealtimeItem();

        JsonParser jsonParser = new JsonParser();
        JsonElement el = jsonParser.parse(str);

        JsonArray jsonArray = null;

        if(el.isJsonArray()){
            jsonArray = el.getAsJsonArray();
            Iterator it = jsonArray.iterator();
            while(it.hasNext()){
                JsonElement e = (JsonElement) it.next();
                Log.d(TAG, e.toString());
            }
        }




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
