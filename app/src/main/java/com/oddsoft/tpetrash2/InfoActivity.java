package com.oddsoft.tpetrash2;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Time;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class InfoActivity extends ActionBarActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Application.class.getSimpleName();

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;

    // Update interval
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    // A fast interval ceiling
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;


    @Bind(R.id.todayView)
    TextView todayView;

    @Bind(R.id.garbageView)
    TextView garbageView;

    @Bind(R.id.foodView)
    TextView foodView;

    @Bind(R.id.recyclingView)
    TextView recyclingView;

    @Bind(R.id.time)
    TextView timeView;

    @Bind(R.id.address)
    TextView addressView;

    @Bind(R.id.carnumber)
    TextView carNumberView;

    @Bind(R.id.memo)
    TextView memoView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private String strToLat = "";
    private String strToLng = "";

    private String address;
    private String carno;
    private String carnumber;
    private String time;
    private String memo;
    private Boolean garbage;
    private Boolean food;
    private Boolean recycling;
    private Boolean realtime;

    // Map fragment
    private GoogleMap map;
    private Analytics ga;

    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_keyboard_backspace)
                .color(Color.WHITE)
                .actionBarSize());

        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        configGoogleApiClient();
        configLocationRequest();
        if (!locationClient.isConnected()) {
            locationClient.connect();
        }

        Bundle bundle = this.getIntent().getExtras();

        strFromLat = bundle.getString("fromLat");
        strFromLng = bundle.getString("fromLng");
        strFrom = strFromLat + "," + strFromLng;

        strToLat = bundle.getString("toLat");
        strToLng = bundle.getString("toLng");
        strTo = strToLat + "," + strToLng;

        address = bundle.getString("address");
        carnumber = bundle.getString("carnumber");
        time = bundle.getString("time");
        memo = bundle.getString("memo");
        garbage = bundle.getBoolean("garbage");
        food = bundle.getBoolean("food");
        recycling = bundle.getBoolean("recycling");
        realtime = bundle.getBoolean("realtime");

        timeView.setText("時間：" + time);
        addressView.setText("地址：" + address);
        carNumberView.setText("車次：" + carnumber);
        memoView.setText("備註：" + memo);

        if (realtime) {
            //新北市垃圾車即時資訊 隱藏這些欄位
            memoView.setVisibility(View.GONE);
            garbageView.setVisibility(View.GONE);
            foodView.setVisibility(View.GONE);
            recyclingView.setVisibility(View.GONE);
        }

        //reset title
        Geocoder geocoder = new Geocoder(this, new Locale("zh", "TW"));
        String current_location = "現在位置:";
        try {
            List<Address> addressList = geocoder.getFromLocation(Double.valueOf(strFromLat)
                    , Double.valueOf(strFromLng), 1);

            if (addressList == null) {
                current_location = getString(R.string.app_name);
            } else {
                current_location = current_location + addressList.get(0).getAdminArea() + "" + addressList.get(0).getLocality();
            }

        } catch (IOException e) {

            Log.d(TAG, e.toString());

            current_location = getString(R.string.app_name);
        }

        getSupportActionBar().setTitle(current_location);

        Time today = new Time();
        todayView.setText("今天是" + today.getDayOfWeekName());


        if (garbage) {
            //今天有收一般垃圾
            garbageView.setText("今天有收一般垃圾");
            garbageView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收一般垃圾"
            garbageView.setText("今天不收一般垃圾");
            garbageView.setTextColor(getResources().getColor(R.color.red));
        }

        if (food) {
            //今天有收廚餘
            foodView.setText("今天有收廚餘");
            foodView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收廚餘
            foodView.setText("今天不收廚餘");
            foodView.setTextColor(getResources().getColor(R.color.red));
        }

        if (recycling) {
            //今天有收資源回收
            recyclingView.setText("今天有收資源回收");
            recyclingView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收資源回收
            recyclingView.setText("今天不收資源回收");
            recyclingView.setTextColor(getResources().getColor(R.color.red));
        }


        // Set up the map fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(strToLat)
                        , Double.valueOf(strToLng)), 17);
        map.animateCamera(center);


        //Current
       /* MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        markerOpt.title("現在位置");
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        map.addMarker(markerOpt).showInfoWindow();*/
        map.setMyLocationEnabled(true);

        //Marker
        MarkerOptions markerOpt2 = new MarkerOptions();
        markerOpt2.position(new LatLng(Double.valueOf(strToLat)
                , Double.valueOf(strToLng)));
        markerOpt2.title(address);
        markerOpt2.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

        map.addMarker(markerOpt2).showInfoWindow();

        //Draw Line
        PolylineOptions polylineOpt = new PolylineOptions();

        LatLng from = new LatLng(Double.valueOf(strFromLat), Double.valueOf(strFromLng));
        LatLng to = new LatLng(Double.valueOf(strToLat), Double.valueOf(strToLng));

        polylineOpt.add(from, to).color(Color.BLUE).width(5);

        line = map.addPolyline(polylineOpt);

        //MapUtils.DrawArrowHead(map, from, to);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
                case R.id.menu_navi:
                    goBrowser();
                    break;

            }

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        MenuItem menuItem1 = menu.findItem(R.id.menu_navi);
        menuItem1.setIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_navigation).actionBarSize().color(Color.WHITE));


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    private void goBrowser() {

        ga.trackEvent(this, "Click", "Button", "Google Map", 0);
        //Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        String from = "saddr=" + strFrom;
        String to = "daddr=" + strTo;
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

    }

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
        if (locationClient != null) {
            locationClient.connect();
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onLocationChanged(Location l2) {

        strFromLat = Double.toString(l2.getLatitude());
        strFromLng = Double.toString(l2.getLongitude());
        strFrom = strFromLat + "," + strFromLng;

        line.remove();

        PolylineOptions polylineOpt = new PolylineOptions();

        LatLng from = new LatLng(l2.getLatitude(), l2.getLongitude());
        LatLng to = new LatLng(Double.valueOf(strToLat), Double.valueOf(strToLng));

        polylineOpt.add(from, to).color(Color.BLUE).width(5);

        line = map.addPolyline(polylineOpt);

        //Log.d(TAG, "onLocationChanged");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection has been connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed");
    }
}
