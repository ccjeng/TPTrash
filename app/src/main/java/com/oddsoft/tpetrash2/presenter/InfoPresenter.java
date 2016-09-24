package com.oddsoft.tpetrash2.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.controller.NewTaipeiOpenDataService;
import com.oddsoft.tpetrash2.model.RealtimeCar;
import com.oddsoft.tpetrash2.presenter.base.BasePresenter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.view.adapter.CustomInfoWindowAdapter;
import com.oddsoft.tpetrash2.view.base.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by andycheng on 2016/9/24.
 */

public class InfoPresenter extends BasePresenter<InfoView> implements OnMapReadyCallback {

    private static final String TAG = InfoPresenter.class.getSimpleName();

    private InfoView view;
    private Context context;

    private String todayInfo;

    private ArrayItem item;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private Double toLat;
    private Double toLng;

    private String city;
    private String address;
    public String time;
    private String memo;
    private String lineid;
    private String lineName;
    private String carNo;
    private Boolean garbage;
    private Boolean food;
    private Boolean recycling;


    private Polyline line;
    private Marker markerCar;

    private Analytics ga;


    public InfoPresenter(InfoView view, Context context){
        this.view = view;
        this.context = context;
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

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity)context);
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

        this.drawLineCar(map);

        //show realtime car
        if (!lineid.equals("")) {
            this.queryRealtimeCar(lineid, map);
        }
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

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity)context);
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
                        Log.e("Error", e.getMessage());
                    }

                    @Override
                    public void onNext(ArrayList<RealtimeCar> realtimeCars) {
                        for(RealtimeCar car: realtimeCars) {

                            if (car.getLineid().equals(lindID)) {

                                try {
                                    Geocoder geocoder = new Geocoder(context, new Locale("zh", "TW"));

                                    String address = car.getLocation();

                                    List<Address> addressList = geocoder.getFromLocationName(address, 1);

                                    Double lat = addressList.get(0).getLatitude();
                                    Double lng = addressList.get(0).getLongitude();

                                    if (lat > 0) {
                                        drawRealTimeCar(gmap, lat, lng
                                                , car.getTime()
                                                , "現在位置在" + car.getLocation() + "\n車號[" +car.getCar() +"] ");
                                    }

                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                }

                            }
                        }

                    }
                });

    }

    public void goBrowser() {

        if (!strFrom.equals(",")) {
            ga.trackEvent((Activity)context, "Click", "Button", "Google Map", 0);
            String from = "saddr=" + strFrom;
            String to = "daddr=" + strTo;
            String para = "&hl=zh&dirflg=w";
            String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
            Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(ie);
        }

    }

    @Override
    public void onCreate() {

        Bundle bundle = ((Activity) context).getIntent().getExtras();

        item = (ArrayItem) ((Activity) context).getIntent().getExtras().getSerializable("item");

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

        if (garbage) {
            todayInfo = strToday+"有收一般垃圾\n";
        }

        if (food) {
            todayInfo = todayInfo + strToday+"有收廚餘\n";
        }

        if (recycling) {
            todayInfo = todayInfo + strToday+"有收資源回收\n";
        }


        view.initView();

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
}
