package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.oddsoft.tpetrash2.realtime.RealtimeItem;
import com.oddsoft.tpetrash2.realtime.RealtimeListAdapter;
import java.util.ArrayList;

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
                        showData(response);
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


    private void showData(String str) {
        ArrayList<RealtimeItem> items = RealtimeItem.fromJson(str, this);
        RealtimeListAdapter adapter = new RealtimeListAdapter(this, items);
        ListView listView = (ListView) findViewById(R.id.listReltimeInfo);
        listView.setAdapter(adapter);
        adapter.addAll(items);
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
