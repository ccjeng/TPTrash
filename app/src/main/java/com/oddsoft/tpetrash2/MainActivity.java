package com.oddsoft.tpetrash2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.android.gms.ads.*;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static boolean getLocationStatus = false;
    private static boolean getNetworkStatus = false;
    private static Double longitude;
    private static Double latitude;
    private static int rownum;
    private static int distance;
    private static int hour;
    private static String sorting;

    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private MyLocationListener mylistener;
    private Context context;
    private ArrayList<TrashItem> result;
    private AdView adView;


    /*
     * Define a request code to send to Google Play services This code is returned in
     * Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    /*
     * Constants for handling location results
     */
    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private static final String PARSE_APPLICATION_ID = "nxkxfDhpFQBXOReTPFIPhGIaYowmT5uuscj3w3Kb";
    private static final String PARSE_CLIENT_KEY = "oo7CwnSrT3XCjVHuN3r1JBw7rvJzjmYZCRCX9e2U";

    // Adapter for the Parse query
    private ParseQueryAdapter<ArrayItem> trashQueryAdapter;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayHomeAsUpEnabled(false);

        // Check Network Status
        if (!isNetworkAvailable()) {
            getNetworkStatus = false;
            showNetworkError();
        } else {
            getNetworkStatus = true;
        }

        // Check GPS Status
        locationServiceInitial();

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        //TODO compare Car Start Time (add CarStartTime column on TaipeiAll object)

        //if (hour < 16)
        //    hour = 16;

        getPref();

        parse();

        if (getNetworkStatus && getLocationStatus) {
            //new HttpGetTask(MainActivity.this).execute();
        }
        //adView();
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
/*        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // 仿真器
                .addTestDevice("7710C21FF2537758BF3F80963477D68E") // 我的 Galaxy Nexus 測試手機
                .build();*/
        adView.loadAd(adRequest);
    }

    private void parse() {
        ParseObject.registerSubclass(ArrayItem.class);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        // Set up a customized query
        ParseQueryAdapter.QueryFactory<ArrayItem> factory =
                new ParseQueryAdapter.QueryFactory<ArrayItem>() {
                    public ParseQuery<ArrayItem> create() {
                        Location myLoc = currentLocation; //(currentLocation == null) ? lastLocation : currentLocation;

                        Log.d(TAG, "location = " + myLoc.toString());

                        ParseQuery<ArrayItem> query = ArrayItem.getQuery();
                        query.whereContains("CarTime", hour + ":");

                        //TODO Sort by distance or car start time
                        //query.orderByDescending("createdAt");

                        query.whereWithinKilometers("location"
                                , geoPointFromLocation(myLoc)
                                , distance
                        );
                        query.setLimit(rownum);
                        return query;
                    }
                };

        // Set up the query adapter
        trashQueryAdapter = new ParseQueryAdapter<ArrayItem>(this, factory) {
            @Override
            public View getItemView(ArrayItem trash, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.trash_item, null);
                }

                super.getItemView(trash, view, parent);

                TextView timeView = (TextView) view.findViewById(R.id.time_view);
                TextView addressView = (TextView) view.findViewById(R.id.address_view);
                TextView distanceView = (TextView) view.findViewById(R.id.distance_view);

                timeView.setText(trash.getCarTime());
                distanceView.setText(trash.getDistance(geoPointFromLocation(currentLocation)).toString());
                addressView.setText(trash.getAddress());

                return view;
            }
        };

        // Disable automatic loading when the adapter is attached to a view.
        trashQueryAdapter.setAutoload(true);

        // Disable pagination, we'll manage the query limit ourselves
        trashQueryAdapter.setPaginationEnabled(false);

        ListView trashListView = (ListView) findViewById(R.id.trashList);
        trashListView.setAdapter(trashQueryAdapter);

        // Set up the handler for an item's selection
        trashListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArrayItem item = trashQueryAdapter.getItem(position);
                //TODO go intent google map
                
            }
        });
    }

    /*
 * Helper method to get the Parse GEO point representation of a location
 */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Prefs.class));
                return true;
            case R.id.exit_settings:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String distancePreference = prefs.getString("distance", "5");
        String rownumPreference = prefs.getString("rownum", "5");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        rownum = Integer.valueOf(rownumPreference);
        sorting = String.valueOf(sortingPreference);

    }

    /*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        goBrowser(result.get(position).getLocation().toString());

    }*/

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
    }
    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }

/*
    private class HttpGetTask extends AsyncTask<Void, Void, ArrayList<TrashItem>> {
        private ProgressDialog dialog;

        private ListActivity activity;

        // private List<Message> messages;
        public HttpGetTask(ListActivity activity) {
            this.activity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        private Context context;

        // Build RESTful trash API
        private String URL = "http://tptrash-api.herokuapp.com/" + +hour + "/"
                + rownum + "/" + distance + "/" + longitude + "/" + latitude;

        AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("處理中");
            this.dialog.show();
        }


        @Override
        protected ArrayList<TrashItem> doInBackground(Void... params) {
            Log.d(TAG, URL);
            HttpGet request = new HttpGet(URL);
            JSONResponseHandler responseHandler = new JSONResponseHandler();
            try {
                return mClient.execute(request, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<TrashItem> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (null != mClient)
                mClient.close();
            setListAdapter(new ArrayAdapter<TrashItem>(
                    MainActivity.this,
                    R.layout.listitem, result));

            if (getListView().getAdapter().getCount() == 0) {
                Toast.makeText(MainActivity.this, R.string.data_not_found, Toast.LENGTH_LONG)
                        .show();
            }
            sorting();

        }
    }

    public class JSONResponseHandler implements ResponseHandler<ArrayList<TrashItem>> {
        private static final String TAG = "JSONResponseHandler";

        @Override
        public ArrayList<TrashItem> handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {
            TrashItem item = null;
            String text = null;
            result = new ArrayList<TrashItem>();

            HttpEntity entity = response.getEntity();
            text = getUTF8ContentFromEntity(entity);

            try {
                String lng = "";
                String lat = "";
                String address = "";
                String carTime = "";
                String location = "";
                String name = "";
                String time = "";

                JsonFactory jsonfactory = new JsonFactory();
                JsonParser jsonParser = jsonfactory.createJsonParser(text);
                JsonToken token = jsonParser.nextToken();

                // ArrayList objectArray = new ArrayList();
                // Expected JSON is an array so if current token is "[" then
                // while
                // we don't get
                // "]" we will keep parsing

                if (token == JsonToken.START_ARRAY) {
                    while (token != JsonToken.END_ARRAY) {
                        // Inside array there are many objects, so it has to
                        // start
                        // with "{" and end with "}"
                        token = jsonParser.nextToken();
                        if (token == JsonToken.START_OBJECT) {
                            while (token != JsonToken.END_OBJECT) {
                                // Each object has a name which we will use to
                                // identify the type.
                                token = jsonParser.nextToken();
                                if (token == JsonToken.FIELD_NAME) {
                                    String fieldname = jsonParser
                                            .getCurrentName();
                                    // Log.d(TAG, fieldname);

                                    if ("Address".equals(fieldname)) {
                                        jsonParser.nextToken();
                                        address = jsonParser.getText();
                                    }

                                    if ("CarTime".equals(fieldname)) {
                                        jsonParser.nextToken();
                                        carTime = jsonParser.getText();
                                    }

                                    if ("Lng".equals(fieldname)) {
                                        jsonParser.nextToken();
                                        lng = jsonParser.getText();
                                    }

                                    if ("Lat".equals(fieldname)) {
                                        jsonParser.nextToken();
                                        lat = jsonParser.getText();
                                    }

                                }

                            }
                            location = lat + "," + lng;
                            name = address.substring(6, address.length());
                            time = carTime;

                            Log.d(TAG, "location=" + location);
                            Log.d(TAG, "name=" + name);
                            Log.d(TAG, "time=" + time);

                            item = new TrashItem(location, time, name);
                            result.add(item);
                        }

                    }

                }
                jsonParser.close();

            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
            }

            return result;
        }

        protected String getUTF8ContentFromEntity(HttpEntity entity)
                throws IllegalStateException, IOException {

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    entity.getContent(), "UTF-8"));
            return reader.readLine();
        }
    }
*/
    private void sorting() {
        if (sorting.equals("TIME")) { //check time sorting
            if (result != null) {
                Collections.sort(result, new Comparator<TrashItem>() {
                    @Override
                    public int compare(TrashItem item1, TrashItem item2) {
                        return item1.getStartTime().compareTo(item2.getStartTime());
                    }
                });
            }
        }
    }

    private void goBrowser(String toLocation) {
        String from = "saddr=" + latitude + "," + longitude;
        String to = "daddr=" + toLocation.toString();
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

    }

    private boolean isNetworkAvailable() {
        final ConnectivityManager connMgr = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable()) {
            return true;
        } else if (mobile.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    private void showNetworkError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.network_error)
                .setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                                // empty
                            }
                        }).show();
    }

    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE); // ���o�t�Ωw���A��
        // Define the criteria how to select the location provider
        Criteria criteria = new Criteria(); // ���T���Ѫ̿����з�
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // default
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // get the best provider depending on the criteria
        bestProvider = lms.getBestProvider(criteria, true); // ���ܺ��ǫ׳̰������Ѫ�
        Location location = lms.getLastKnownLocation(bestProvider);

        mylistener = new MyLocationListener();

        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        // location updates: at least 5 meter and 1000 minsecs change
        lms.requestLocationUpdates(bestProvider, 1000, 5, mylistener);

    }

private class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;

        // Initialize the location fields
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        getLocationStatus = true;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Toast.makeText(MainActivity.this,
        // provider + "'s status changed to " + status + "!",
        // Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Toast.makeText(MainActivity.this,
        // "Provider " + provider + " enabled!", Toast.LENGTH_SHORT)
        // .show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        // Toast.makeText(MainActivity.this,
        // "Provider " + provider + " disabled!", Toast.LENGTH_SHORT)
        // .show();
    }
}
}
