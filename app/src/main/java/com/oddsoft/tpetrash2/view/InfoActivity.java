package com.oddsoft.tpetrash2.view;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.Application;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.adapter.ArrayItem;
import com.oddsoft.tpetrash2.adapter.RealtimeGson;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class InfoActivity extends AppCompatActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = InfoActivity.class.getSimpleName();

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;

    // Update interval
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    // A fast interval ceiling
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;

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

    @Bind(R.id.memo)
    TextView memoView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private ArrayItem item;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private String strToLat = "";
    private String strToLng = "";

    private String city;
    private String address;
    private String time;
    private String memo;
    private String lineid;
    private String lineName;
    private String carNo;
    private Boolean garbage;
    private Boolean food;
    private Boolean recycling;

    // Map fragment
    private GoogleMap map;
    private Analytics ga;

    private Polyline line;
    private Marker markerCar;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        city = bundle.getString("city");
        address = bundle.getString("address");
        lineName = bundle.getString("line");
        carNo = bundle.getString("carno");
        time = bundle.getString("time");
        memo = bundle.getString("memo");
        lineid = bundle.getString("lineid");
        garbage = bundle.getBoolean("garbage");
        food = bundle.getBoolean("food");
        recycling = bundle.getBoolean("recycling");

        addressView.setText("地址：" + address);
        memoView.setText("備註：" + memo);

        //set toolbar title
        getSupportActionBar().setTitle(time);

        String strToday = Time.getDayOfWeekName();

        if (garbage) {
            //今天有收一般垃圾
            garbageView.setText(strToday + "有收一般垃圾");
            garbageView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收一般垃圾"
            garbageView.setText(strToday + "不收一般垃圾");
            garbageView.setTextColor(getResources().getColor(R.color.red));
        }

        if (food) {
            //今天有收廚餘
            foodView.setText(strToday + "有收廚餘");
            foodView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收廚餘
            foodView.setText(strToday + "不收廚餘");
            foodView.setTextColor(getResources().getColor(R.color.red));
        }

        if (recycling) {
            //今天有收資源回收
            recyclingView.setText(strToday + "有收資源回收");
            recyclingView.setTextColor(getResources().getColor(R.color.green));
        } else {
            //今天沒收資源回收
            recyclingView.setText(strToday + "不收資源回收");
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
        map.setMyLocationEnabled(true);

        //show realtime car
        if (!lineid.equals("")) {
            //query lineid from realtime data set, and draw it on the map.
            queryRealtimeJson(lineid);
        }


        //Marker
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(Double.valueOf(strToLat), Double.valueOf(strToLng)))
                .title(address)
                .snippet(time)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

        map.addMarker(markerOpt).showInfoWindow();

        //Draw Line
        PolylineOptions polylineOpt = new PolylineOptions();

        LatLng from = new LatLng(Double.valueOf(strFromLat), Double.valueOf(strFromLng));
        LatLng to = new LatLng(Double.valueOf(strToLat), Double.valueOf(strToLng));

        polylineOpt.add(from, to).color(Color.BLUE).width(5);

        line = map.addPolyline(polylineOpt);

        drawLineCar();
        adView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        MenuItem menuItem1 = menu.findItem(R.id.menu_navi);
        menuItem1.setIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_navigation).actionBar().color(Color.WHITE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_navi:
                goBrowser();
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    private void goBrowser() {

        ga.trackEvent(this, "Click", "Button", "Google Map", 0);
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

        if (adView != null)
            adView.pause();

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

        if (adView != null)
            adView.resume();

        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null)
            adView.destroy();
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

    private void drawLineCar(){

        AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

        query.whereEqualTo("line", lineName);
        query.whereNotEqualTo("address", address);

        if (city.equals("Taipei")) {
            query.whereEqualTo("carno", carNo);
        }

        query.findInBackground(new com.avos.avoscloud.FindCallback<ArrayItem>() {
            @Override
            public void done(List<ArrayItem> item, AVException e) {
                for(ArrayItem i: item) {
                    //Marker
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(new LatLng(i.getLocation().getLatitude()
                            , i.getLocation().getLongitude()));
                    markerOption.title(i.getAddress())
                            .snippet(i.getCarTime());
                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                    markerCar = map.addMarker(markerOption);
                }
            }
        });

    }

    private void drawRealTimeCar(Double lat, Double lng, String time, String address) {

        //Marker
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(lat, lng));
        markerOption.title(address);
        markerOption.snippet(time);
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_truck));

        map.addMarker(markerOption);

    }


    private void queryRealtimeJson(final String lindID) {

        RequestQueue mQueue = Volley.newRequestQueue(this);

        String url = "http://data.ntpc.gov.tw/od/data/api/28AB4122-60E1-4065-98E5-ABCCB69AACA6?$format=json";

        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();

                        Type listType = new TypeToken<ArrayList<RealtimeGson>>() {
                        }.getType();
                        ArrayList<RealtimeGson> jsonArr = gson.fromJson(response, listType);

                        for (RealtimeGson obj : jsonArr) {
                            Log.d(TAG, obj.getLineid() +'-' + obj.getLocation());

                            if (obj.getLineid().equals(lindID)) {

                                try {
                                    Geocoder geocoder = new Geocoder(InfoActivity.this, new Locale("zh", "TW"));

                                    String address = obj.getLocation();

                                    List<Address> addressList = geocoder.getFromLocationName(address, 1);

                                    Double lat = addressList.get(0).getLatitude();
                                    Double lng = addressList.get(0).getLongitude();

                                    if (lat > 0) {
                                        drawRealTimeCar(lat, lng, obj.getTime(), "[" +obj.getCar() +"] " + obj.getLocation());
                                    }

                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }

                            }
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        });

        mQueue.add(stringRequest);


    }

    private void adView() {

        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_INFO);
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

}
