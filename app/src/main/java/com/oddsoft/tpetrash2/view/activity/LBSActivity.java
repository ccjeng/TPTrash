package com.oddsoft.tpetrash2.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.view.adapter.ArrayItemAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.BaseActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LBSActivity extends BaseActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LBSActivity.class.getSimpleName();
    public static final String INTENT_EXTRA_ITEM = "intentItem";

    private static int distance;
    private static int hour;
    private static int currentHour;
    private static String sorting;

    @Bind(R.id.day_spinner)
    Spinner daySpinner;
    @Bind(R.id.hour_spinner)
    Spinner hourSpinner;
    @Bind(R.id.sort_spinner)
    Spinner sortSpinner;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorlayout;

    private AdView adView;

    private String[] dayCode;
    private String[] dayName;
    private String[] hourCode;
    private String[] hourName;
    private String[] sortCode;
    private String[] sortName;
    private int today;
    private String selectedDay;
    private String selectedHour;
    private String selectedSort;
    private boolean queryRunnung;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    /*
     * Define a request code to send to Google Play services This code is returned in
     * Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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
    private Analytics ga;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    private ArrayItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbs);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        getPref();

        initActionBar();

        initSpinner();

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        today = Integer.valueOf(Time.getDayOfWeekNumber());

        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        hour = currentHour;

        //set hour spinner to current hour
        if (hour < 5) {
            hour = 5;
        }

        if (Utils.isNetworkConnected(this)) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }

        } else {

            Utils.showSnackBar(coordinatorlayout, getString(R.string.network_error), Utils.Mode.ERROR);
        }

        adView();

    }

    private void initSpinner() {
        dayCode = getResources().getStringArray(R.array.day_spinnner_code);
        dayName = getResources().getStringArray(R.array.day_spinnner_name);
        hourCode = getResources().getStringArray(R.array.hour_spinnner_code);
        hourName = getResources().getStringArray(R.array.hour_spinnner_name);
        sortCode = getResources().getStringArray(R.array.pref_sorting_code);
        sortName = getResources().getStringArray(R.array.pref_sorting_item);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this, R.array.day_spinnner_name,
                android.R.layout.simple_spinner_item);

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
                this, R.array.hour_spinnner_name,
                android.R.layout.simple_spinner_item);

        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.pref_sorting_item,
                android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(Arrays.asList(sortCode).indexOf(sorting));
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }


    private void spinnerSelected() {
        selectedDay = dayCode[daySpinner.getSelectedItemPosition()];
        selectedHour = hourCode[hourSpinner.getSelectedItemPosition()];
        selectedSort = sortCode[sortSpinner.getSelectedItemPosition()];

        if (hourSpinner.getSelectedItemPosition()!=0 &&
                !queryRunnung) {
            runQuery(Integer.valueOf(selectedDay)
                    , Integer.valueOf(selectedHour)
                    , selectedSort);

            //show 3/7 messages
            if (selectedDay.equals("3") || selectedDay.equals("0")) {
                Toast.makeText(this, Time.getDayOfWeekName(Integer.valueOf(selectedDay))+"，台北市沒有收垃圾，新北市僅部分區域有收垃圾！"
                        ,Toast.LENGTH_LONG).show();
            }
        }

    }

    private void adView() {

        LinearLayout adBannerLayout = (LinearLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_LBS);
        adView.setAdSize(AdSize.SMART_BANNER);
        adBannerLayout.addView(adView);

        AdRequest adRequest;

        if (Application.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Constant.ADMOB_TEST_DEVICE_ID)
                    .build();
        } else {

            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);

    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void runQuery(final int day, final int hour, final String sort) {

        Log.d(TAG, day + " - " + hour + " - " + sort);

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;


        if (myLoc != null) {

            //set current location to global veriable
            Application.setCurrentLocation(myLoc);

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            String strHour = String.valueOf(hour);
            String weekTag = Utils.getWeekTag(day);

            Log.d(TAG, "weekTag = " + weekTag);
            AVGeoPoint userLocation = new AVGeoPoint(myLoc.getLatitude(), myLoc.getLongitude());
            AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

            if (sort.equals("TIME")) {
                query.orderByAscending("time");
            }

            query.whereEqualTo(weekTag, "Y")
                    .whereEqualTo("hour",  strHour)
                    .whereWithinKilometers("location", userLocation, distance)
                    .setLimit(100);

            progressWheel.setVisibility(View.VISIBLE);

            queryRunnung = true;
            query.findInBackground(new FindCallback<ArrayItem>() {
                public void done(List<ArrayItem> avObjects, AVException e) {

                    progressWheel.setVisibility(View.GONE);

                    if (e == null) {

                        adapter = new ArrayItemAdapter(LBSActivity.this, avObjects, String.valueOf(day), currentHour, myLoc);

                        recyclerView.setAdapter(adapter);

                        adapter.setOnItemClickListener(new ArrayItemAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(ArrayItem item) {
                                Log.d(TAG, item.getAddress());
                                goIntent(item);
                            }
                        });

                        if (avObjects.size() == 0) {
                            Utils.showSnackBar(coordinatorlayout, getString(R.string.data_not_found), Utils.Mode.INFO);
                        }

                    } else {
                        Utils.showSnackBar(coordinatorlayout, getString(R.string.network_error), Utils.Mode.ERROR);
                    }

                    queryRunnung = false;

                }
            });


        } else {
            //location error
            Utils.showSnackBar(coordinatorlayout, getString(R.string.location_error), Utils.Mode.ERROR);

        }


    }

    /*
 * Helper method to get the Parse GEO point representation of a location
 */
    private AVGeoPoint geoPointFromLocation(Location loc) {
        return new AVGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String distancePreference = prefs.getString("distance", "1");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        if (distance > 10) {
            distance = 10;
        }
        sorting = String.valueOf(sortingPreference);
    }

    @Override
    public void onStop() {
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect to the location services client
        if (locationClient != null) {
            locationClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();

        // 移除位置請求服務
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        locationClient, this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPref();
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


    private void goIntent(ArrayItem item) {

        ga.trackEvent(this, "Location", "Region", item.getRegion(), 0);
        ga.trackEvent(this, "Location", "Address", item.getFullAddress(), 0);

        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("item", (Parcelable) item);

        Bundle bundle = new Bundle();
        bundle.putString("fromLat", String.valueOf(myLoc.getLatitude()));
        bundle.putString("fromLng", String.valueOf(myLoc.getLongitude()));
        bundle.putString("selectedDay", selectedDay);
        intent.putExtras(bundle);

        startActivity(intent);

    }


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

        //call Parse service to get data

        if (Application.getRefreshFlag()) {
            daySpinner.setSelection(Arrays.asList(dayCode).indexOf(String.valueOf(today)));
            hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));

            Application.setRefreshFlag(false);
        }


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
            Utils.showSnackBar(coordinatorlayout, getString(R.string.google_play_service_missing), Utils.Mode.ERROR);

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
