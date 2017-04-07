package com.oddsoft.tpetrash2.presenter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.controller.LocationService;
import com.oddsoft.tpetrash2.controller.NewTaipeiOpenDataService;
import com.oddsoft.tpetrash2.controller.TaipeiOpenDataService;
import com.oddsoft.tpetrash2.model.NPRecycle;
import com.oddsoft.tpetrash2.model.TPCloth.TPCloth;
import com.oddsoft.tpetrash2.model.TPFix.TPFix;
import com.oddsoft.tpetrash2.model.TPFood.TPFood;
import com.oddsoft.tpetrash2.presenter.base.BasePresenter;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.CustomInfoWindowAdapter;
import com.oddsoft.tpetrash2.view.base.Application;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by andycheng on 2016/10/3.
 */

public class CustomMapPresenter extends BasePresenter<CustomMapView>
        implements OnMapReadyCallback, LocationConnectedListener {

    private static final String TAG = CustomMapPresenter.class.getSimpleName();

    private CustomMapView view;
    private Context context;
    private LocationService locationService;
    public Location currentLocation;
    private String mapType;

    public CustomMapPresenter(CustomMapView view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);

        float zoom = 14;
        switch (mapType) {
            case "tpfix":  //台北市資源回收及廚餘限時收受點
                drawLocationTPFix(map);
                break;
            case "tpfood": //台北市週三、週日廚餘專用限時收受點
                drawLocationTPFood(map);
                break;
            case "tpcloth": //台北市舊衣回收箱
                drawLocationTPCloth(map);
                zoom = 16;
                break;
            case "ntrecycle": //新北市黃金資收站設置資訊
                drawLocationNTRecycle(map);
                break;
        }



        if (currentLocation != null) {
            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom));

        } else {
            view.showError(context.getString(R.string.location_error), Utils.Mode.ERROR);
        }
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
                .baseUrl(Constant.GITHUB_GIST)
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
                        view.showError(context.getString(R.string.data_error), Utils.Mode.ERROR);
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
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity) context);
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
                .baseUrl(Constant.GITHUB_GIST)
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
                        view.showError(context.getString(R.string.data_error), Utils.Mode.ERROR);
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
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity) context);
                            gmap.setInfoWindowAdapter(adapter);

                            gmap.addMarker(markerOption);
                        }
                    }
                });

    }

    private void drawLocationTPCloth(final GoogleMap gmap) {
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

        TaipeiOpenDataService service = retrofit.create(TaipeiOpenDataService.class);

        service.getTaipeiClothLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TPCloth>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Error", e.getMessage());
                        view.showError(context.getString(R.string.data_error), Utils.Mode.ERROR);
                    }

                    @Override
                    public void onNext(TPCloth tpCloths) {

                        for(int i=0; i<tpCloths.getResult().getResults().size(); i++){

                            String team = tpCloths.getResult().getResults().get(i).getBranch();
                            String address = tpCloths.getResult().getResults().get(i).getAddress();
                            //String tel = tpCloths.getResult().getResults().get(i).getTel();
                            Double lat = Double.valueOf(tpCloths.getResult().getResults().get(i).getLat());
                            Double lng = Double.valueOf(tpCloths.getResult().getResults().get(i).getLng());

                            //Marker
                            MarkerOptions markerOption = new MarkerOptions();
                            markerOption.position(new LatLng(lat, lng));
                            markerOption.title(team);
                            markerOption.snippet(address);
                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity) context);
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
                        view.showError(context.getString(R.string.data_error), Utils.Mode.ERROR);
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

                                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

                                CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity) context);
                                gmap.setInfoWindowAdapter(adapter);
                                gmap.addMarker(markerOption);
                            }

                        }

                    }
                });
    }


    @Override
    public void onLocationServiceConnected(Location location) {
        Log.d(TAG, "onLocationServiceConnected");

        currentLocation = location;

        //auto set selected selection on spinner
        if (currentLocation != null) {
            MapFragment mapFragment = ((MapFragment) ((Activity) context).getFragmentManager().findFragmentById(R.id.map_fragment));
            mapFragment.getMapAsync(this);
        } else {
            //location error
            view.showError(context.getString(R.string.location_error), Utils.Mode.ERROR);
        }

    }

    @Override
    public void onCreate() {
        view.initView();
        locationService = new LocationService(context);
        locationService.setLocationConnectedListener(this);

        if (Utils.isNetworkConnected(context)) {
            locationService.connect();
        } else {
            view.showError(context.getString(R.string.network_error), Utils.Mode.ERROR);
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        locationService.disconnect();
    }

    @Override
    public void onResume() {
        locationService.connect();
    }

    @Override
    public void onPause() {
        locationService.pause();
    }

    @Override
    public void onDestroy() {

    }
}
