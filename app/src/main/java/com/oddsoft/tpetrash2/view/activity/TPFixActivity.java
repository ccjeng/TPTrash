package com.oddsoft.tpetrash2.view.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.controller.TaipeiOpenDataService;
import com.oddsoft.tpetrash2.model.TPFix.TPFix;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.BaseActivity;

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

public class TPFixActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = TPFixActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    MapFragment mapFragment;

    private Analytics ga;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpfix);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        initActionBar();
        adView();

        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment));
        mapFragment.getMapAsync(this);

    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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


    private void drawLocation(final GoogleMap gmap) {
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
                    }

                    @Override
                    public void onNext(TPFix tpFixs) {

                        //Log.d(TAG, "size = "+tpFixs.getResult().getResults().size());

                        for(int i=0; i<tpFixs.getResult().getResults().size(); i++){
                            //Log.d(TAG, i + " - " + tpFixs.getResult().getResults().get(i).getAddress());

                            String team = tpFixs.getResult().getResults().get(i).getBranch();
                            String address = tpFixs.getResult().getResults().get(i).getAddress();
                            String memo = tpFixs.getResult().getResults().get(i).getMemo();
                            Double lat = Double.valueOf(tpFixs.getResult().getResults().get(i).getLat());
                            Double lng = Double.valueOf(tpFixs.getResult().getResults().get(i).getLng());

                            //Marker
                            MarkerOptions markerOption = new MarkerOptions();
                            markerOption.position(new LatLng(lat, lng));
                            markerOption.title(team + " - " + address);
                            markerOption.snippet(memo);
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.bullet_red));

                            gmap.addMarker(markerOption);
                        }
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);

        drawLocation(map);
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

}
