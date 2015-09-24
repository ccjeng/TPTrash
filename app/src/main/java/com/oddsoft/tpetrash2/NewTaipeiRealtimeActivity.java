package com.oddsoft.tpetrash2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.realtime.RealtimeOItem;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by andycheng on 2015/8/11.
 */
public class NewTaipeiRealtimeActivity extends ActionBarActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "NewTaipeiRealtime";

    private GoogleMap map;


    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Analytics ga;
    private AdView adView;
    public static final int REFRESH_DELAY = 1000;
    //private VpadnBanner vponBanner = null;

    /*
     * Constants for location update parameters
     */
    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;
    private Location myLoc;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    private ParseQueryAdapter<RealtimeOItem> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtaipeirealtime);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_keyboard_backspace)
                .color(Color.WHITE)
                .actionBarSize());


        ga = new Analytics();
        ga.trackerPage(this);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        //get current location from global veriable
        currentLocation = Application.getCurrentLocation();

        if (currentLocation != null) {
            CameraUpdate center =
                    CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude()
                            , currentLocation.getLongitude()), 15);
            map.animateCamera(center);
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setCompassEnabled(true);


        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }


            getData();

        } else {
            //Network error
            Toast.makeText(NewTaipeiRealtimeActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();

        }

        adView();

    }

    private void getData() {
        myLoc = (currentLocation == null) ? lastLocation : currentLocation;


        //fake location
/*
        if (Application.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(25.175579);
            //myLoc.setLongitude(121.43847);

            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }
*/
        if (myLoc != null) {

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            //clear map markers
            map.clear();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("RealTime");
            query.setLimit(300);
            //query.whereWithinKilometers("location", geoPointFromLocation(myLoc), 100);

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> items, ParseException e) {

                    if (e == null) {
                        int i = 0;
                        for (i = 0; i < items.size(); i++) {
                            //Marker
                            MarkerOptions marker = new MarkerOptions();
                            marker.position(new LatLng(items.get(i).getParseGeoPoint("location").getLatitude()
                                       , items.get(i).getParseGeoPoint("location").getLongitude()))
                                    .title(items.get(i).get("address").toString())
                                    .snippet(items.get(i).get("cartime").toString())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_truck));
                            map.addMarker(marker);
                        }

                    } else {
                        Log.d(TAG, "Error: " + e.getMessage());
                    }

                }
            });


        } else {
            //location error
            Toast.makeText(NewTaipeiRealtimeActivity.this, R.string.location_error, Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.realtime, menu);

        MenuItem menuItem1 = menu.findItem(R.id.menu_refresh);
        menuItem1.setIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_refresh).actionBarSize().color(Color.WHITE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_refresh:
                getData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
* Called when the Activity is no longer visible at all. Stop updates and disconnect.
*/
    @Override
    public void onStop() {
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
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
        if (locationClient!=null) {
            if (locationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, NewTaipeiRealtimeActivity.this);
            }
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
        super.onDestroy();
    }

    private void adView() {
        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Application.ADMOB_UNIT_ID);
        adView.setAdSize(AdSize.SMART_BANNER);
        adBannerLayout.addView(adView);

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
            Toast.makeText(NewTaipeiRealtimeActivity.this, R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
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
