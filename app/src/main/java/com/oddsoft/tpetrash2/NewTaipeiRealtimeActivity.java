package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oddsoft.tpetrash2.realtime.JsonService;
import com.oddsoft.tpetrash2.realtime.RealtimeItem;
import com.oddsoft.tpetrash2.realtime.RealtimeListAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


/**
 * Created by andycheng on 2015/8/11.
 */
public class NewTaipeiRealtimeActivity extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "NewTaipeiRealtime";
    protected ProgressDialog proDialog;

    @Bind(R.id.listRealtimeInfo)
    ListView listView;

    @Bind(R.id.pull_to_refresh)
    SwipeRefreshLayout mSwipeLayout;

    private Analytics ga;
    private AdView adView;
    public static final int REFRESH_DELAY = 1000;

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

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;
    private Location myLoc;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    private RealtimeListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtaipeirealtime);
        ButterKnife.bind(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ga = new Analytics();
        ga.trackerPage(this);

        //getData();

        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }

            //get current location from global veriable
            currentLocation = Application.getCurrentLocation();
            getData();

        } else {
            Crouton.makeText(NewTaipeiRealtimeActivity.this, R.string.network_error, Style.ALERT).show();
        }

        adView();

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    private void getData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://data.ntpc.gov.tw/od/data/api/28AB4122-60E1-4065-98E5-ABCCB69AACA6?$format=json";

        proDialog = new ProgressDialog(this);
        proDialog.setMessage(getString(R.string.processing) + " (因為來源資料要再做地理資訊的計算，若資料量多，會比較耗時，不便之處請見諒)");
        proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        proDialog.setCancelable(false);
        proDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showData(response);
                        proDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                proDialog.dismiss();
                Log.d(TAG, error.toString());
            }

        });

        queue.add(stringRequest);

    }


    private void showData(String str) {
        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        //fake location

        if (Application.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(25.175579);
            //myLoc.setLongitude(121.43847);

            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }

        if (myLoc != null) {

            JsonService jsonsrv = new JsonService(NewTaipeiRealtimeActivity.this
                    , myLoc.getLatitude()
                    , myLoc.getLongitude());
            ArrayList<RealtimeItem> items = jsonsrv.fromJson(str);
            listAdapter = new RealtimeListAdapter(this, items);
            listView.setAdapter(listAdapter);

            // Set up the handler for an item's selection
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final RealtimeItem item = listAdapter.getItem(position);
                    //Open Detail Page
                    goIntent(item);
                }
            });
            if (listAdapter.getCount() == 0) {
                //Toast.makeText(NewTaipeiRealtimeActivity.this, "沒有資料", Toast.LENGTH_LONG).show();
                Crouton.makeText(NewTaipeiRealtimeActivity.this, "沒有資料", Style.CONFIRM).show();
            } else {
                //Descending Order
                Collections.sort(items, new Comparator<RealtimeItem>() {
                    @Override
                    public int compare(RealtimeItem o1,
                                       RealtimeItem o2) {
                        return Double.compare(o1.getDistance(), o2.getDistance()); // error
                    }
                });
            }

        } else {
            //location error
            Crouton.makeText(NewTaipeiRealtimeActivity.this, R.string.location_error, Style.ALERT).show();
        }
    }

    /*
        SwipeRefreshLayout
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
                mSwipeLayout.setRefreshing(false);
            }
        }, REFRESH_DELAY);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            // case R.id.menu_refresh:
            //     getData();
            //     return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
* Called when the Activity is no longer visible at all. Stop updates and disconnect.
*/
    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            locationClient.disconnect();
        }
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        // Connect to the location services client
        if (locationClient != null) {
            locationClient.connect();
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();

        // 移除位置請求服務
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, NewTaipeiRealtimeActivity.this);
        }
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();

        // 連線到Google API用戶端
        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;

        if (Application.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Application.ADMOB_TEST_DEVICE_ID)
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);
    }

    private void goIntent(RealtimeItem item) {

        ga.trackEvent(this, "Location", "RealTimeLocation", item.getCarLocation(), 0);


        Intent intent = new Intent();
        intent.setClass(this, InfoActivity.class);
        Bundle bundle = new Bundle();

        bundle.putBoolean("realtime", true);
        bundle.putString("fromLat", String.valueOf(myLoc.getLatitude()));
        bundle.putString("fromLng", String.valueOf(myLoc.getLongitude()));
        bundle.putString("toLat", String.valueOf(item.getLatitude()));
        bundle.putString("toLng", String.valueOf(item.getLongitude()));

        bundle.putString("address", item.getCarLocation());
        //bundle.putString("carno", item.getCarNO());
        bundle.putString("carnumber", item.getCarNO());
        bundle.putString("time", item.getCarTime());

        intent.putExtras(bundle);

        if (item.getDistance() != 999999999) {
            startActivityForResult(intent, 0);
        }

    }

    /*
* check network state
* */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /*
* Verify that Google Play services is available before making a request.
*
* @return true if Google Play services is available, otherwise false
*/
    private boolean isGoogleServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Application.APPDEBUG) {
                Log.d(TAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d(TAG, "Google play services NOT available");
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
    }

    /*
* Get the current location
*/
    private Location getLocation() {
        // If Google Play Services is available
        if (isGoogleServicesAvailable()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    //Google Play Service ConnectionCallbacks
    // 已經連線到Google Services
    @Override
    public void onConnected(Bundle bundle) {
        if (Application.APPDEBUG)
            Log.d(TAG, "onConnected - Connected to location services");


        currentLocation = getLocation();

        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);

        if (Application.APPDEBUG)
            Log.d(TAG, "onConnected - isConnected =" + locationClient.isConnected());

        //getData();

    }

    // Google Services連線中斷
    // int參數是連線中斷的代號
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    // Google Services連線失敗
    // ConnectionResult參數是連線失敗的資訊
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        Log.i(TAG, "GoogleApiClient connection failed");

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Crouton.makeText(NewTaipeiRealtimeActivity.this, R.string.google_play_service_missing, Style.ALERT).show();
        }

    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private void configLocationRequest() {
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use low power
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }

    // 位置改變
    // Location參數是目前的位置
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
    }


}
