package com.oddsoft.tpetrash2.view.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.adapter.CustomInfoWindowAdapter;
import com.oddsoft.tpetrash2.controller.NewTaipeiOpenDataService;
import com.oddsoft.tpetrash2.controller.TaipeiOpenDataService;
import com.oddsoft.tpetrash2.model.NPRecycle;
import com.oddsoft.tpetrash2.model.TPFix.TPFix;
import com.oddsoft.tpetrash2.model.TPFood.TPFood;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.BaseActivity;

import java.util.ArrayList;

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

public class TPFixActivity extends BaseActivity
        implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = TPFixActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.coordinatorlayout)
    RelativeLayout coordinatorlayout;

    MapFragment mapFragment;

    private Analytics ga;
    private AdView adView;
    private Location currentLocation;
    private GoogleApiClient locationClient;
    private LocationRequest locationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;
    private String mapType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpfix);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        Bundle bundle = this.getIntent().getExtras();

        mapType = bundle.getString("mapType");

        initActionBar();
        adView();

        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment));

        if (Utils.isNetworkConnected(this)) {
            configGoogleApiClient();
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }


        } else {
            Utils.showSnackBar(coordinatorlayout, getString(R.string.network_error), Utils.Mode.ERROR);

        }

    }

    private void initActionBar() {
        setSupportActionBar(toolbar);

        String title = "";
        switch (mapType) {
            case "tpfix":  //台北市資源回收及廚餘限時收受點
                title = getString(R.string.tpfix);
                break;
            case "tpfood": //台北市週三、週日廚餘專用限時收受點
                title = getString(R.string.tpfood);
                break;
            case "ntrecycle": //新北市黃金資收站設置資訊
                title = getString(R.string.ntrecycle);
                break;
         }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(title);

        }
    }

    private void adView() {

        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_MAIN);
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


    private void drawLocationTPFix(final GoogleMap gmap) {
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
                .baseUrl(Constant.TAIPEI_OPENDATA)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient)
                .build();

        TaipeiOpenDataService service = retrofit.create(TaipeiOpenDataService.class);

        service.getTaipeiFixLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TPFix>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Error", e.getMessage());
                        Utils.showSnackBar(coordinatorlayout, getString(R.string.data_error), Utils.Mode.ERROR);
                    }

                    @Override
                    public void onNext(TPFix tpFixs) {

                        for(int i=0; i<tpFixs.getResult().getResults().size(); i++){

                            String team = tpFixs.getResult().getResults().get(i).getBranch();
                            String address = tpFixs.getResult().getResults().get(i).getAddress();
                            String memo = tpFixs.getResult().getResults().get(i).getMemo();
                            String phone = tpFixs.getResult().getResults().get(i).getPhone();
                            Double lat = Double.valueOf(tpFixs.getResult().getResults().get(i).getLat());
                            Double lng = Double.valueOf(tpFixs.getResult().getResults().get(i).getLng());

                            //Marker
                            MarkerOptions markerOption = new MarkerOptions();
                            markerOption.position(new LatLng(lat, lng));
                            markerOption.title(team + "\n" + address);
                            markerOption.snippet(memo + "\n" + phone);
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(TPFixActivity.this);
                            gmap.setInfoWindowAdapter(adapter);

                            gmap.addMarker(markerOption);
                        }
                    }
                });

    }


    private void drawLocationTPFood(final GoogleMap gmap) {
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
                .baseUrl(Constant.TAIPEI_OPENDATA)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient)
                .build();

        TaipeiOpenDataService service = retrofit.create(TaipeiOpenDataService.class);

        service.getTaipeiFoodLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TPFood>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Error", e.getMessage());
                        Utils.showSnackBar(coordinatorlayout, getString(R.string.data_error), Utils.Mode.ERROR);
                    }

                    @Override
                    public void onNext(TPFood tpFoods) {

                        for(int i=0; i<tpFoods.getResult().getResults().size(); i++){

                            String team = tpFoods.getResult().getResults().get(i).getBranch();
                            String address = tpFoods.getResult().getResults().get(i).getAddress();
                            String memo = tpFoods.getResult().getResults().get(i).getMemo();
                            Double lat = Double.valueOf(tpFoods.getResult().getResults().get(i).getLat());
                            Double lng = Double.valueOf(tpFoods.getResult().getResults().get(i).getLng());

                            //Marker
                            MarkerOptions markerOption = new MarkerOptions();
                            markerOption.position(new LatLng(lat, lng));
                            markerOption.title(team + "\n" + address);
                            markerOption.snippet(memo);
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(TPFixActivity.this);
                            gmap.setInfoWindowAdapter(adapter);

                            gmap.addMarker(markerOption);
                        }
                    }
                });

    }


    private void drawLocationNTRecycle(final GoogleMap gmap) {
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
                .baseUrl(Constant.GITHUB_GIST)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient)
                .build();

        NewTaipeiOpenDataService service = retrofit.create(NewTaipeiOpenDataService.class);

        service.getNewTaipeiRecycleLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<NPRecycle>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        Log.e(TAG, e.toString());
                        Utils.showSnackBar(coordinatorlayout, getString(R.string.data_error), Utils.Mode.ERROR);
                    }

                    @Override
                    public void onNext(ArrayList<NPRecycle> items) {
                        for(NPRecycle item: items) {

                            String recycle_address = item.getRecycleAddress();
                            String address = item.getAddress();
                            String name = item.getName();
                            String village = item.getVillage();
                            String tel = item.getTel();
                            String open_time = item.getOpenTime().replace("\n"," ");
                            String state = item.getState();

                            Double lat = 0.0;
                            Double lng = 0.0;
                            if (!item.getWgs84aY().trim().equals("")) {
                                lat = Double.valueOf(item.getWgs84aY());
                                lng = Double.valueOf(item.getWgs84aX());

                                //Marker
                                MarkerOptions markerOption = new MarkerOptions();
                                markerOption.position(new LatLng(lat, lng));
                                markerOption.title(village + " " + recycle_address);
                                markerOption.snippet(open_time + " \n"+ state + " \n電話 : " + tel +
                                        "\n里長姓名 : " + name + "\n里辦地址 : " +address);

                                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                                CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(TPFixActivity.this);
                                gmap.setInfoWindowAdapter(adapter);
                                gmap.addMarker(markerOption);
                            }

                        }

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);

        switch (mapType) {
            case "tpfix":  //台北市資源回收及廚餘限時收受點
                drawLocationTPFix(map);
                break;
            case "tpfood": //台北市週三、週日廚餘專用限時收受點
                drawLocationTPFood(map);
                break;
            case "ntrecycle": //新北市黃金資收站設置資訊
                drawLocationNTRecycle(map);
                break;
        }



        if (currentLocation != null) {
            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14));

        } else {
            Utils.showSnackBar(coordinatorlayout, getString(R.string.location_error), Utils.Mode.ERROR);
        }
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    @Override
    public void onStart() {
        super.onStart();
        if (locationClient != null) {
            locationClient.connect();
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

        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
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
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null)
            adView.destroy();
    }

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
    public void onConnected(@Nullable Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(locationClient);

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
