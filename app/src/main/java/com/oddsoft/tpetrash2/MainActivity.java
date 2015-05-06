package com.oddsoft.tpetrash2;

import com.google.android.gms.ads.*;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;


import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Application.APPTAG;
    private static int rownum;
    private static int distance;
    private static int hour;
    private static String sorting;
    private AdView adView;
    protected ProgressDialog proDialog;
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

    // Adapter for the Parse query
    private ParseQueryAdapter<ArrayItem> trashQueryAdapter;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayHomeAsUpEnabled(false);

        // 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        //TODO compare Car Start Time (add CarStartTime column on TaipeiAll object)

        //if (hour < 16)
        //    hour = 16;

        hour = 16;

        getPref();

        //parseAdapter();
        //adView();
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;

        if (Application.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("7710C21FF2537758BF3F80963477D68E")
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);
    }

    private void parseQuery() {

        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        // Set up a customized query
        ParseQueryAdapter.QueryFactory<ArrayItem> factory =
                new ParseQueryAdapter.QueryFactory<ArrayItem>() {
                    public ParseQuery<ArrayItem> create() {
                        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

                        //Log.d(TAG, "location = " + myLoc.toString());
                        ParseQuery<ArrayItem> query = ArrayItem.getQuery();

                        //String[] hours = {String.valueOf(hour) + "", String.valueOf(hour+1) + ""};
                        //query.whereContainedIn("CarTime", Arrays.asList(hours));

                        query.whereContains("CarTime", String.valueOf(hour) + ":");
                        //query.whereContains("CarTime", String.valueOf(hour+1) + ":");

                        //TODO Sort by distance or car start time
                        //query.orderByDescending("createdAt");
                        if (sorting.equals("TIME")) {
                            query.orderByDescending("CarTime");
                        }
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

                TextView timeView = (TextView) view.findViewById(R.id.time_view);
                TextView addressView = (TextView) view.findViewById(R.id.address_view);
                TextView distanceView = (TextView) view.findViewById(R.id.distance_view);

                timeView.setText(trash.getCarTime());
                distanceView.setText(trash.getDistance(geoPointFromLocation(currentLocation)).toString());
                addressView.setText(trash.getAddress());

                return view;
            }
        };


        trashQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ArrayItem>() {

            @Override
            public void onLoading() {
                //To change body of implemented methods use File | Settings | File Templates.
                Log.w(TAG, "onLoading");
                proDialog = new ProgressDialog(MainActivity.this);
                proDialog.setMessage("loading...");
                proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                proDialog.setCancelable(false);
                proDialog.show();
            }

            @Override
            public void onLoaded(List<ArrayItem> objects, Exception e) {
                //To change body of implemented methods use File | Settings | File Templates.

                if (proDialog != null && proDialog.isShowing())
                    proDialog.dismiss();

            }
        });



    }

    private void parseAdapter() {

        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        // Disable automatic loading when the adapter is attached to a view.
        //trashQueryAdapter.setAutoload(true);

        // Disable pagination, we'll manage the query limit ourselves
        //trashQueryAdapter.setPaginationEnabled(false);

        ListView trashListView = (ListView) findViewById(R.id.trashList);
        trashListView.setAdapter(trashQueryAdapter);

        // Set up the handler for an item's selection
        trashListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArrayItem item = trashQueryAdapter.getItem(position);
                //Open Google Map
                goBrowser(String.valueOf(item.getLocation().getLatitude()) + "," +
                        String.valueOf(item.getLocation().getLongitude()));
            }
        });

        /*
        if (trashListView.getCount() == 0) {
            int msg;

            if (myLoc == null) {
                //unable to get current location
                msg = R.string.location_error;
            } else {
                //no data
                msg = R.string.data_not_found;
            }
            new AlertDialog.Builder(this)
                    //.setTitle(R.string.app_name)
                    .setMessage(R.string.data_not_found)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();

        }*/

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
    * Called when the Activity is no longer visible at all. Stop updates and disconnect.
    */
    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            locationClient.disconnect();
        }
        super.onStop();
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        // Connect to the location services client
        locationClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();

        // 移除位置請求服務
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    locationClient, this);
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
        if (!locationClient.isConnected()) {
            locationClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }


    private void goBrowser(String toLocation) {

        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        String from = "saddr=" + myLoc.getLatitude() + "," + myLoc.getLongitude();
        String to = "daddr=" + toLocation.toString();
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

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
                Log.d(Application.APPTAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d(Application.APPTAG, "Google play services NOT  available");
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
        if (Application.APPDEBUG) {
            Log.d(TAG, "Connected to location services");
        }

        currentLocation = getLocation();

        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);

        //call Parse service to get data
        parseQuery();
        parseAdapter();
    }

    // Google Services連線中斷
    // int參數是連線中斷的代號
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Application.APPTAG, "GoogleApiClient connection has been suspend");
    }

    // Google Services連線失敗
    // ConnectionResult參數是連線失敗的資訊
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        Log.i(Application.APPTAG, "GoogleApiClient connection failed");

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            new AlertDialog.Builder(this)
                    //.setTitle(R.string.app_name)
                    .setMessage(R.string.google_play_service_missing)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();
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


    private void configLocationRequest() {
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
