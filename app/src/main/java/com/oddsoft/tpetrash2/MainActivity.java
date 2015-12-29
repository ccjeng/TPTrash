package com.oddsoft.tpetrash2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.utils.Utils;
import com.parse.ParseAnalytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends AppCompatActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Application.class.getSimpleName();
    private static int distance;
    private static int hour;
    private static String sorting;

    @Bind(R.id.hour_spinner)
    Spinner hourSpinner;

    @Bind(R.id.trashList)
    ListView trashListView;

    @Bind(R.id.navigation)
    NavigationView navigation;

    @Bind(R.id.drawerlayout)
    DrawerLayout drawerLayout;

    private AdView adView;
    private String[] hourCode;
    private String[] hourName;

    private ActionBar actionbar;

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

    // Adapter for the Parse query
    private ParseQueryAdapter<ArrayItem> trashQueryAdapter;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;
    private Location myLoc;
    private Analytics ga;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    private static final int DIALOG_WELCOME = 1;
    private static final int DIALOG_UPDATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        initActionBar();
        initDrawer();
        showPushNotification();

        hourCode = getResources().getStringArray(R.array.hour_spinnner_code);
        hourName = getResources().getStringArray(R.array.hour_spinnner_name);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.hour_spinnner_name,
                android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(adapter);

        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                String hr = hourCode[hourSpinner.getSelectedItemPosition()];

                if (!hr.equals("5")) {
                    parseQuery(Integer.valueOf(hr));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (Utils.isNewInstallation(this)) {
            this.showDialog(DIALOG_WELCOME);
        } else
        if (Utils.newVersionInstalled(this)) {
            this.showDialog(DIALOG_UPDATE);
        }

        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        } else {
            Crouton.makeText(MainActivity.this, R.string.network_error, Style.ALERT,
                    (ViewGroup)findViewById(R.id.croutonview)).show();
        }
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        //set hour spinner to current hour
        if (hour < 5) {
            hour = 5;
        }

        //show 3/7 messages
        if (Time.getDayOfWeekNumber().equals("3") || Time.getDayOfWeekNumber().equals("0")) {
            Toast.makeText(this,"今天是"+Time.getDayOfWeekName()+"，台北市沒有收垃圾，新北市僅部分區域有收垃圾！"
                    ,Toast.LENGTH_LONG).show();
        }

        getPref();
        adView();
    }

    private void adView() {

        LinearLayout adBannerLayout = (LinearLayout) findViewById(R.id.footerLayout);

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


    private void initActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_menu)
                .color(Color.WHITE)
                .actionBar());

        actionbar = getSupportActionBar();

    }

    private void initDrawer() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navRecycle:
                        startActivity(new Intent(MainActivity.this, RecycleActivity.class));
                        break;
                    case R.id.navRealtime:
                        startActivity(new Intent(MainActivity.this, NewTaipeiRealtimeActivity.class));
                        break;
                    case R.id.navSetting:
                        startActivity(new Intent(MainActivity.this, Prefs.class));
                        break;
                    case R.id.navAbout:
                        new LibsBuilder()
                                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .withAboutAppName(getString(R.string.app_name))
                                .withActivityTitle(getString(R.string.about))
                                .withAboutDescription(getString(R.string.license))
                                        //start the activity
                                .start(MainActivity.this);
                        break;
                    case R.id.navSuggest:
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.oddsoft.tpetrash2")));
                        break;

                }
                return false;
            }
        });

        //change navigation drawer item icons
        navigation.getMenu().findItem(R.id.navRecycle).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_recycle)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navRealtime).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_truck)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navSetting).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_cog)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navAbout).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navSuggest).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_thumbs_up)
                .color(Color.GRAY)
                .sizeDp(24));
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void parseQuery(final int hour) {

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        if (myLoc != null) {

            //set current location to global veriable
            Application.setCurrentLocation(myLoc);

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            // Set up a customized query
            ParseQueryAdapter.QueryFactory<ArrayItem> factory =
                    new ParseQueryAdapter.QueryFactory<ArrayItem>() {
                        public ParseQuery<ArrayItem> create() {

                            String strHour = String.valueOf(hour);
                            String wkFood = Utils.getWeekFoodTag();
                            String wkGarbage = Utils.getWeekGarbageTag();
                            String wkRecycling = Utils.getWeekRecyclingTag();

                            if (Application.APPDEBUG) {
                                Log.d(TAG, "hour = " + strHour);
                                Log.d(TAG, "wkFood = " + wkFood);
                                Log.d(TAG, "wkGarbage = " + wkGarbage);
                                Log.d(TAG, "wkRecycling = " + wkRecycling);
                            }

                            ParseQuery<ArrayItem> foodscrap = ArrayItem.getQuery();
                            foodscrap.whereEqualTo(wkFood, "Y");

                            ParseQuery<ArrayItem> garbage = ArrayItem.getQuery();
                            garbage.whereEqualTo(wkGarbage, "Y");

                            ParseQuery<ArrayItem> recycling = ArrayItem.getQuery();
                            recycling.whereEqualTo(wkRecycling, "Y");

                            List<ParseQuery<ArrayItem>> queries = new ArrayList<ParseQuery<ArrayItem>>();
                            queries.add(foodscrap);
                            queries.add(garbage);
                            queries.add(recycling);

                            ParseQuery finalQuery = ParseQuery.or(queries);

                            if (sorting.equals("TIME")) {
                                finalQuery.orderByAscending("time");
                            }


                            finalQuery.whereEqualTo("hour", strHour);
                            finalQuery.whereWithinKilometers("location"
                                    , geoPointFromLocation(myLoc)
                                    , distance
                            );

                            finalQuery.setLimit(50);

                            return finalQuery;
                        }
                    };

            // Set up the query adapter
            //todo change to recyclerview + cardview
            trashQueryAdapter = new ParseQueryAdapter<ArrayItem>(this, factory) {
                @Override
                public View getItemView(ArrayItem trash, View view, ViewGroup parent) {
                    if (view == null) {
                        view = View.inflate(getContext(), R.layout.trash_item, null);
                    }

                    TextView timeView = (TextView) view.findViewById(R.id.time_view);
                    TextView addressView = (TextView) view.findViewById(R.id.address_view);
                    TextView distanceView = (TextView) view.findViewById(R.id.distance_view);

                    TextView garbageView = (TextView) view.findViewById(R.id.garbage_view);
                    TextView foodView = (TextView) view.findViewById(R.id.food_view);
                    TextView recyclingView = (TextView) view.findViewById(R.id.recycling_view);

                    timeView.setText(trash.getCarTime());
                    distanceView.setText(trash.getDistance(geoPointFromLocation(myLoc)).toString());
                    addressView.setText(trash.getAddress());

                    if (trash.checkTodayAvailableGarbage()) {
                        garbageView.setText("[收一般垃圾]");
                        garbageView.setTextColor(getResources().getColor(R.color.green));

                    } else {
                        garbageView.setText("[不收一般垃圾]");
                        garbageView.setTextColor(getResources().getColor(R.color.red));
                    }

                    if (trash.checkTodayAvailableFood()) {
                        foodView.setText(" [收廚餘]");
                        foodView.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        foodView.setText(" [不收廚餘]");
                        foodView.setTextColor(getResources().getColor(R.color.red));
                    }

                    if (trash.checkTodayAvailableRecycling()) {
                        recyclingView.setText(" [收資源回收]");
                        recyclingView.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        recyclingView.setText(" [不收資源回收]");
                        recyclingView.setTextColor(getResources().getColor(R.color.red));
                    }


                    return view;
                }
            };

            trashQueryAdapter.setPaginationEnabled(false);
            trashQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ArrayItem>() {

                @Override
                public void onLoading() {
                    progressWheel.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded(List<ArrayItem> objects, Exception e) {

                    progressWheel.setVisibility(View.GONE);

                    //No data
                    if (trashListView.getCount() == 0) {
                        String msg = String.valueOf(distance) + "公里"
                                + getString(R.string.data_not_found);

                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        Crouton.makeText(MainActivity.this, msg, Style.CONFIRM,
                                (ViewGroup)findViewById(R.id.croutonview)).show();

                    }
                }
            });

            trashListView.setAdapter(trashQueryAdapter);

            // Set up the handler for an item's selection
            trashListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ArrayItem item = trashQueryAdapter.getItem(position);
                    //Open Detail Page
                    goIntent(item);
                }
            });

        } else {
            //location error
            Crouton.makeText(MainActivity.this, R.string.location_error, Style.ALERT,
                    (ViewGroup)findViewById(R.id.croutonview)).show();
        }


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

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
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

    //show push notification message
    private void showPushNotification() {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("alert")) {
                String msg = extras.getString("alert");

                if (!msg.equals("")) {

                    // Linkify the message
                    final SpannableString msgWithLinkify = new SpannableString(msg);
                    Linkify.addLinks(msgWithLinkify, Linkify.ALL);

                    final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                            .setMessage(msgWithLinkify)
                            .setPositiveButton(R.string.ok_label,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                            // empty
                                        }
                                    }).show();
                    // Make the textview clickable
                    ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                }
            }
        }

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
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        locationClient, this);
            }
        }
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
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

        Crouton.cancelAllCroutons();
        super.onDestroy();
    }


    private void goIntent(ArrayItem item) {

        ga.trackEvent(this, "Location", "Region", item.getRegion(), 0);
        ga.trackEvent(this, "Location", "Address", item.getFullAddress(), 0);


        Intent intent = new Intent();
        intent.setClass(this, InfoActivity.class);
        Bundle bundle = new Bundle();

        bundle.putBoolean("realtime", false);
        bundle.putString("fromLat", String.valueOf(myLoc.getLatitude()));
        bundle.putString("fromLng", String.valueOf(myLoc.getLongitude()));
        bundle.putString("toLat", String.valueOf(item.getLocation().getLatitude()));
        bundle.putString("toLng", String.valueOf(item.getLocation().getLongitude()));

        bundle.putString("address", item.getFullAddress());
        bundle.putString("time", item.getCarTime());
        bundle.putBoolean("garbage", item.checkTodayAvailableGarbage());
        bundle.putBoolean("food", item.checkTodayAvailableFood());
        bundle.putBoolean("recycling", item.checkTodayAvailableRecycling());
        bundle.putString("memo", item.getMemo());
        bundle.putString("lineid", item.getLineID());

        intent.putExtras(bundle);

        startActivityForResult(intent, 0);

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

        //call Parse service to get data
        //parseQuery(hour);

        if (Application.getRefreshFlag()) {
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
            Crouton.makeText(MainActivity.this, R.string.google_play_service_missing, Style.ALERT).show();
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

    protected final Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_info_circle)
                        .color(Color.GRAY)
                        .sizeDp(24));

                builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);

        final Context context = this;

        switch (id) {
            case DIALOG_WELCOME:
                builder.setTitle(getResources().getString(R.string.welcome_title));
                builder.setMessage(getResources().getString(R.string.welcome_message));
                break;
            case DIALOG_UPDATE:
                builder.setTitle(getString(R.string.changelog_title));
                final String[] changes = getResources().getStringArray(R.array.updates);
                final StringBuilder buf = new StringBuilder();
                for (int i = 0; i < changes.length; i++) {
                    buf.append("\n\n");
                    buf.append(changes[i]);
                }
                builder.setMessage(buf.toString().trim());
                break;
        }
        return builder.create();
    }

}
