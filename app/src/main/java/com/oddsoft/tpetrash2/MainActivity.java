package com.oddsoft.tpetrash2;

import com.google.android.gms.ads.*;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Application.APPTAG;
    private static int distance;
    private static int hour;
    private static String sorting;
    private AdView adView;
    private ListView trashListView;
    private Spinner hourSpinner;
    private String[] hourCode;
    private String[] hourName;
    protected ProgressDialog proDialog;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mLlvDrawerContent;
    private ListView mLsvDrawerMenu;

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

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Analytics ga = new Analytics();
       if (!Application.APPDEBUG)
            ga.initTracker(this);

        initActionBar();
        initDrawer();
        initDrawerList();

        trashListView = (ListView) findViewById(R.id.trashList);
        hourCode = getResources().getStringArray(R.array.hour_spinnner_code);
        hourName = getResources().getStringArray(R.array.hour_spinnner_name);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.hour_spinnner_name,
                android.R.layout.simple_spinner_item);
        hourSpinner = (Spinner) findViewById(R.id.hour_spinnner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(adapter);

        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                String hr = hourCode[hourSpinner.getSelectedItemPosition()];

                if (!hr.equals("12")) {
                    parseQuery(Integer.valueOf(hr));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
        else{
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.network_error)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();

        }
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        //set hour spinner to current hour
        if (hour < 12)
                hour = 12;
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


    private void initActionBar(){
        //顯示 Up Button (位在 Logo 左手邊的按鈕圖示)
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //打開 Up Button 的點擊功能
        getActionBar().setHomeButtonEnabled(true);
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drw_layout);
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
                getActionBar().setTitle(R.string.app_name);
            }

            //被關上後要做的事情
            @Override
            public void onDrawerClosed(View drawerView) {
                // 將 Title 設定回 APP 的名稱
                getActionBar().setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initDrawerList() {

        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        // 定義新宣告的兩個物件：選項清單的 ListView 以及 Drawer內容的 LinearLayou
        mLsvDrawerMenu = (ListView) findViewById(R.id.lsv_drawer_menu);
        mLlvDrawerContent = (LinearLayout) findViewById(R.id.llv_left_drawer);

        int[] iconImage = { android.R.drawable.ic_menu_preferences
                , android.R.drawable.ic_dialog_info };

        List<HashMap<String,String>> lstData = new ArrayList<HashMap<String,String>>();
        for (int i = 0; i < iconImage.length; i++) {
            HashMap<String, String> mapValue = new HashMap<String, String>();
            mapValue.put("icon", Integer.toString(iconImage[i]));
            mapValue.put("title", drawer_menu[i]);
            lstData.add(mapValue);
        }


        SimpleAdapter adapter = new SimpleAdapter(this, lstData
                , R.layout.drawer_item
                , new String[]{"icon", "title"}
                , new int[]{R.id.rowIcon, R.id.rowText});
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
                startActivity(new Intent(this, Prefs.class));
                break;
            case 1:
                startActivity(new Intent(this, AboutActivity.class));
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
            //myLoc.setLatitude(25.175579);
            //myLoc.setLongitude(121.43847);

            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }

        if (myLoc != null ) {

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            // Set up a customized query
            ParseQueryAdapter.QueryFactory<ArrayItem> factory =
                    new ParseQueryAdapter.QueryFactory<ArrayItem>() {
                        public ParseQuery<ArrayItem> create() {

                            ParseQuery<ArrayItem> query = ArrayItem.getQuery();

                            query.whereContains("CarTime", String.valueOf(hour) + ":");

                            if (sorting.equals("TIME")) {
                                query.orderByAscending("CarTime");
                            }

                            query.whereWithinKilometers("location"
                                    , geoPointFromLocation(myLoc)
                                    , distance
                            );

                            query.setLimit(100);

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

                    TextView garbageView =  (TextView) view.findViewById(R.id.garbage_view);
                    TextView foodView =  (TextView) view.findViewById(R.id.food_view);
                    TextView recyclingView =  (TextView) view.findViewById(R.id.recycling_view);

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
                    proDialog = new ProgressDialog(MainActivity.this);
                    proDialog.setMessage("資料擷取中");
                    proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    proDialog.setCancelable(false);
                    proDialog.show();
                }

                @Override
                public void onLoaded(List<ArrayItem> objects, Exception e) {
                    //To change body of implemented methods use File | Settings | File Templates.

                    if (proDialog != null && proDialog.isShowing())
                        proDialog.dismiss();

                    //No data
                    if (trashListView.getCount() == 0) {
                        String msg = hourName[Arrays.asList(hourCode).indexOf(String.valueOf(hour))]
                                + " " + String.valueOf(distance) + "公里"
                                + getString(R.string.data_not_found);

                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(msg)
                                .setPositiveButton(R.string.ok_label,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialoginterface, int i) {
                                                // empty
                                            }
                                        }).show();
                    }
                }
            });

            trashListView.setAdapter(trashQueryAdapter);

            // Set up the handler for an item's selection
            trashListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ArrayItem item = trashQueryAdapter.getItem(position);
                    //Open Google Map
                    goIntent(item);
                    //goBrowser(String.valueOf(item.getLocation().getLatitude()) + "," +
                    //        String.valueOf(item.getLocation().getLongitude()));
                }
            });

        }
        else {
            //location error
            new AlertDialog.Builder(MainActivity.this)
                    //.setTitle(R.string.app_name)
                    .setMessage(R.string.location_error)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();
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


    /*
    * Called when the Activity is no longer visible at all. Stop updates and disconnect.
    */
    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            locationClient.disconnect();
        }
        super.onStop();

        if (!Application.APPDEBUG)
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
        if (!Application.APPDEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
        Intent intent = new Intent();
        intent.setClass(this, InfoActivity.class);
        Bundle bundle = new Bundle();

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
        bundle.putString("carno", item.getCarNo());
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
    private boolean isNetworkConnected(){
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
                Log.d(Application.APPTAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d(Application.APPTAG, "Google play services NOT available");
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
        hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));

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
