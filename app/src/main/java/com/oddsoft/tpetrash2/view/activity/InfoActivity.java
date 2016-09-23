package com.oddsoft.tpetrash2.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.view.adapter.CustomInfoWindowAdapter;
import com.oddsoft.tpetrash2.controller.NewTaipeiOpenDataService;
import com.oddsoft.tpetrash2.model.RealtimeCar;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class InfoActivity extends BaseActivity
        implements OnMapReadyCallback {

    private static final String TAG = InfoActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private ArrayItem item;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private Double toLat;
    private Double toLng;

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
    private MapFragment mapFragment;
    private Analytics ga;

    private Polyline line;
    private Marker markerCar;

    private AdView adView;
    private String todayInfo;
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

        Bundle bundle = this.getIntent().getExtras();

        item = (ArrayItem) getIntent().getExtras().getSerializable("item");

        strFromLat = bundle.getString("fromLat");
        strFromLng = bundle.getString("fromLng");
        strFrom = strFromLat + "," + strFromLng;
        toLat = item.getLocation().getLatitude();
        toLng = item.getLocation().getLongitude();

        strTo = item.getLocation().getLatitude() + "," + item.getLocation().getLongitude();

        city = item.getCity();
        address = item.getFullAddress();
        lineName = item.getLine();
        carNo = item.getCarNo();
        time = item.getCarTime();

        String selectedDay = bundle.getString("selectedDay");
        String strToday = Time.getDayOfWeekName(Integer.valueOf(selectedDay));

        memo = item.getMemo(selectedDay);
        lineid = item.getLineID();
        garbage = item.checkTodayAvailableGarbage(selectedDay);
        food = item.checkTodayAvailableFood(selectedDay);
        recycling = item.checkTodayAvailableRecycling(selectedDay);

        //set toolbar title
        getSupportActionBar().setTitle(time);

        if (garbage) {
            todayInfo = strToday+"有收一般垃圾\n";
        }

        if (food) {
            todayInfo = todayInfo + strToday+"有收廚餘\n";
        }

        if (recycling) {
            todayInfo = todayInfo + strToday+"有收資源回收\n";
        }

        // Set up the map fragment
        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment));
        mapFragment.getMapAsync(this);


        adView();
    }


    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(toLat, toLng), 17);
        map.animateCamera(center);
        map.setMyLocationEnabled(true);

        //Marker
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(toLat, toLng))
                .title(address)
               // .snippet(time + "\n\n" + memo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

        markerOpt.snippet(time + "\n\n" + todayInfo + memo);

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(InfoActivity.this);
        map.setInfoWindowAdapter(adapter);
        map.addMarker(markerOpt).showInfoWindow();

        //Draw Line
        PolylineOptions polylineOpt = new PolylineOptions();

        if (!strFromLat.equals("")) {
            LatLng from = new LatLng(Double.valueOf(strFromLat), Double.valueOf(strFromLng));
            LatLng to = new LatLng(toLat, toLng);

            polylineOpt.add(from, to).color(Color.BLUE).width(5);

            line = map.addPolyline(polylineOpt);
        }

        drawLineCar(map);

        //show realtime car
        if (!lineid.equals("")) {
            queryRealtimeCar(lineid, map);
        }

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

        if (!strFrom.equals(",")) {
            ga.trackEvent(this, "Click", "Button", "Google Map", 0);
            String from = "saddr=" + strFrom;
            String to = "daddr=" + strTo;
            String para = "&hl=zh&dirflg=w";
            String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
            Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(ie);
        }

    }


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
        super.onDestroy();

        if (adView != null)
            adView.destroy();
    }

    private void drawLineCar(final GoogleMap gmap){

        AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

        query.whereEqualTo("line", lineName);
        query.whereNotEqualTo("address", address);

        if (city.equals("Taipei")) {
            query.whereEqualTo("carno", carNo);
        }

        query.findInBackground(new com.avos.avoscloud.FindCallback<ArrayItem>() {
            @Override
            public void done(List<ArrayItem> item, AVException e) {
                if (item != null) {
                    for (ArrayItem i : item) {
                        //Marker
                        MarkerOptions markerOption = new MarkerOptions();
                        markerOption.position(new LatLng(i.getLocation().getLatitude()
                                , i.getLocation().getLongitude()));
                        markerOption.title(i.getAddress())
                                .snippet(i.getCarTime());
                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                        markerCar = gmap.addMarker(markerOption);
                    }
                }
            }
        });

    }

    private void drawRealTimeCar(GoogleMap gmap, Double lat, Double lng, String time, String address) {

        //Marker
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(lat, lng));
        markerOption.title(address);
        markerOption.snippet(time);
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_truck));

        gmap.addMarker(markerOption);

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(InfoActivity.this);
        gmap.setInfoWindowAdapter(adapter);

    }


    private void queryRealtimeCar(final String lindID, final GoogleMap gmap) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (Application.APPDEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient okhttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.NEWTAIPEI_OPENDATA)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient)
                .build();

        NewTaipeiOpenDataService service = retrofit.create(NewTaipeiOpenDataService.class);

        service.getRealTimeCar()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<RealtimeCar>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Error", e.getMessage());
                    }

                    @Override
                    public void onNext(ArrayList<RealtimeCar> realtimeCars) {
                        for(RealtimeCar car: realtimeCars) {

                            if (car.getLineid().equals(lindID)) {

                                try {
                                    Geocoder geocoder = new Geocoder(InfoActivity.this, new Locale("zh", "TW"));

                                    String address = car.getLocation();

                                    List<Address> addressList = geocoder.getFromLocationName(address, 1);

                                    Double lat = addressList.get(0).getLatitude();
                                    Double lng = addressList.get(0).getLongitude();

                                    if (lat > 0) {
                                        drawRealTimeCar(gmap, lat, lng, car.getTime(), "現在位置在" + car.getLocation() + "\n車號[" +car.getCar() +"] ");
                                    }

                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }

                            }
                        }

                    }
                });

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
