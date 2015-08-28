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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.drawer.DrawerItem;
import com.oddsoft.tpetrash2.drawer.DrawerItemAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Utils;
import com.parse.ParseAnalytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends ActionBarActivity
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

    @Bind(R.id.lsv_drawer_menu)
    ListView mLsvDrawerMenu;

    @Bind(R.id.llv_left_drawer)
    LinearLayout mLlvDrawerContent;

    @Bind(R.id.drw_layout)
    DrawerLayout mDrawerLayout;

    private AdView adView;
    private String[] hourCode;
    private String[] hourName;

    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate pbLoading;

    // 記錄被選擇的選單指標用
    private int mCurrentMenuItemPosition = -1;

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

    private boolean stopAutoSelection = false;

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
        initDrawerList();
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
                // TODO Auto-generated method stub

            }
        });

        if (Utils.isNewInstallation(this)) {
            stopAutoSelection = true;
            this.showDialog(DIALOG_WELCOME);
        } else
        if (Utils.newVersionInstalled(this)) {
            stopAutoSelection = true;
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
        //set default value
        //hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));

        getPref();
        adView();
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


    private void initActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_menu)
                .color(Color.WHITE)
                .actionBarSize());

        actionbar = getSupportActionBar();
    }

    private void initDrawer() {
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drw_layout);
        // 設定 Drawer 的影子
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
                R.drawable.ic_drawer, // Drawer 的 Icon
                R.string.app_name, // Drawer 被打開時的描述
                R.string.app_name // Drawer 被關閉時的描述
        ) {
            //被打開後要做的事情
            @Override
            public void onDrawerOpened(View drawerView) {
                // 將 Title 設定為自定義的文字
                actionbar.setTitle(R.string.app_name);
            }

            //被關上後要做的事情
            @Override
            public void onDrawerClosed(View drawerView) {
                // 將 Title 設定回 APP 的名稱
                actionbar.setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initDrawerList() {

        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        DrawerItem[] drawerItem = new DrawerItem[5];

        drawerItem[0] = new DrawerItem(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_recycle)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[0]);
        drawerItem[1] = new DrawerItem(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_truck)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[1]);
        drawerItem[2] = new DrawerItem(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_cog)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[2]);
        drawerItem[3] = new DrawerItem(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[3]);
        drawerItem[4] = new DrawerItem(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_thumbs_up)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[4]);

        DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_item, drawerItem);
        mLsvDrawerMenu.setAdapter(adapter);

        // 當清單選項的子物件被點擊時要做的動作
        mLsvDrawerMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(position);
            }
        });

    }

    private void selectMenuItem(int position) {
        mCurrentMenuItemPosition = position;

        switch (mCurrentMenuItemPosition) {
            case 0:
                startActivity(new Intent(this, RecycleActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, NewTaipeiRealtimeActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, Prefs.class));
                break;
            case 3:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case 4:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.oddsoft.tpetrash2")));
                break;
        }

        // 將選單的子物件設定為被選擇的狀態
        mLsvDrawerMenu.setItemChecked(position, true);

        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void parseQuery(final int hour) {

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        //fake location
        if (Application.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(24.8979347);
            //myLoc.setLongitude(121.5393508);
            //myLoc.setLatitude(25.0249034);
            //myLoc.setLongitude(121.560214);
            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }

        if (myLoc != null) {

            //set current location to global veriable
            Application.setCurrentLocation(myLoc);

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            // Set up a customized query
            ParseQueryAdapter.QueryFactory<ArrayItem> factory =
                    new ParseQueryAdapter.QueryFactory<ArrayItem>() {
                        public ParseQuery<ArrayItem> create() {

                            String strHour;
                            if (String.valueOf(hour).length() == 1) {
                                strHour = "0" + String.valueOf(hour);
                            } else {
                                strHour = String.valueOf(hour);
                            }
/*
                            ParseQuery<ArrayItem> query = ArrayItem.getQuery();
                            query.whereContains("time", strHour + ":");
                            //query.whereEqualTo("foodscraps_mon", "N");
                            //query.whereEqualTo("garbage_mon", "N");
                            //query.whereEqualTo("recycling_mon", "N");
*/
                            String wkFood = Utils.getWeekFoodTag();
                            String wkGarbage = Utils.getWeekGarbageTag();
                            String wkRecycling = Utils.getWeekRecyclingTag();

                            if (Application.APPDEBUG) {
                                Log.d(TAG, "wkFood = " + wkFood);
                                Log.d(TAG, "wkGarbage = " + wkGarbage);
                                Log.d(TAG, "wkRecycling = " + wkRecycling);
                            }

                            ParseQuery<ArrayItem> foodscrap = ArrayItem.getQuery();
                            foodscrap.whereContains("time", strHour + ":");
                            foodscrap.whereEqualTo(wkFood, "Y");

                            ParseQuery<ArrayItem> garbage = ArrayItem.getQuery();
                            garbage.whereContains("time", strHour + ":");
                            garbage.whereEqualTo(wkGarbage, "Y");

                            ParseQuery<ArrayItem> recycling = ArrayItem.getQuery();
                            recycling.whereContains("time", strHour + ":");
                            recycling.whereEqualTo(wkRecycling, "Y");

                            List<ParseQuery<ArrayItem>> queries = new ArrayList<ParseQuery<ArrayItem>>();
                            queries.add(foodscrap);
                            queries.add(garbage);
                            queries.add(recycling);

                            ParseQuery finalQuery = ParseQuery.or(queries);

                            if (sorting.equals("TIME")) {
                                finalQuery.orderByAscending("time");
                            }

                            finalQuery.whereWithinKilometers("location"
                                    , geoPointFromLocation(myLoc)
                                    , distance
                            );

                            finalQuery.setLimit(100);

                            return finalQuery;
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

                    TextView garbageView = (TextView) view.findViewById(R.id.garbage_view);
                    TextView foodView = (TextView) view.findViewById(R.id.food_view);
                    TextView recyclingView = (TextView) view.findViewById(R.id.recycling_view);

                    timeView.setText(trash.getCarTime());
                    distanceView.setText(trash.getDistance(geoPointFromLocation(myLoc)).toString());
                    addressView.setText(trash.getAddress());

                    if (trash.checkTodayAvailableGarbage()) {
                        garbageView.setText("[今天有收一般垃圾]");
                        garbageView.setTextColor(getResources().getColor(R.color.green));

                    } else {
                        garbageView.setText("[今天不收一般垃圾]");
                        garbageView.setTextColor(getResources().getColor(R.color.red));
                    }

                    if (trash.checkTodayAvailableFood()) {
                        foodView.setText(" [今天有收廚餘]");
                        foodView.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        foodView.setText(" [今天不收廚餘]");
                        foodView.setTextColor(getResources().getColor(R.color.red));
                    }

                    if (trash.checkTodayAvailableRecycling()) {
                        recyclingView.setText(" [今天有收資源回收]");
                        recyclingView.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        recyclingView.setText(" [今天不收資源回收]");
                        recyclingView.setTextColor(getResources().getColor(R.color.red));
                    }


                    return view;
                }
            };

            trashQueryAdapter.setPaginationEnabled(false);
            trashQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ArrayItem>() {

                @Override
                public void onLoading() {
                    pbLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded(List<ArrayItem> objects, Exception e) {

                    pbLoading.setVisibility(View.GONE);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String distancePreference = prefs.getString("distance", "3");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        sorting = String.valueOf(sortingPreference);
    }

    //show push notification message
    private void showPushNotification() {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("alert")) {
                stopAutoSelection = true;
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

//        ArrayList list = new ArrayList();
//        list.add(item);
//        bundle.putParcelableArrayList("list", list);


        //bundle.putString("to", String.valueOf(item.getLocation().getLatitude()) + "," +
        //        String.valueOf(item.getLocation().getLongitude()));
        bundle.putString("address", item.getFullAddress());
        //bundle.putString("carno", item.getCarNo());
        bundle.putString("carnumber", item.getCarNumber());
        bundle.putString("time", item.getCarTime());
        bundle.putBoolean("garbage", item.checkTodayAvailableGarbage());
        bundle.putBoolean("food", item.checkTodayAvailableFood());
        bundle.putBoolean("recycling", item.checkTodayAvailableRecycling());
        bundle.putString("memo", item.getMemo());

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
        if (!stopAutoSelection) {
            hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));
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
