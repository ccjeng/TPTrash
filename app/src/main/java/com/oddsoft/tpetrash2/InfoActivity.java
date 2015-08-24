package com.oddsoft.tpetrash2;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Time;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class InfoActivity extends FragmentActivity {

    private static final String TAG = Application.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();

        /*
        ArrayList list = bundle.getParcelableArrayList("list");

        ArrayItem item = (ArrayItem) list.get(0);

        address = item.getAddress();
        carno = item.getCarNo();
        carnumber = item.getCarNumber();
        time = item.getCarTime();
        memo = item.getMemo();
        garbage = item.checkTodayAvailableGarbage();
        food = item.checkTodayAvailableFood();
        recycling = item.checkTodayAvailableRecycling();


        strTo = String.valueOf(item.getLocation().getLatitude()) + "," +
                String.valueOf(item.getLocation().getLongitude());

*/

        strFromLat = bundle.getString("fromLat");
        strFromLng = bundle.getString("fromLng");
        strFrom = strFromLat + "," + strFromLng;

        strToLat = bundle.getString("toLat");
        strToLng = bundle.getString("toLng");
        strTo = strToLat + "," + strToLng;

        address = bundle.getString("address");
        //carno = bundle.getString("carno");
        carnumber = bundle.getString("carnumber");
        time = bundle.getString("time");
        memo = bundle.getString("memo");
        garbage = bundle.getBoolean("garbage");
        food = bundle.getBoolean("food");
        recycling = bundle.getBoolean("recycling");
        realtime = bundle.getBoolean("realtime");

        timeView.setText("時間：" + time);
        addressView.setText("地址：" + address);
        //carNoView.setText("車號：" + carno);
        carNumberView.setText("車次：" + carnumber);
        memoView.setText("備註：" + memo);

        if (realtime) {
            //新北市垃圾車即時資訊 隱藏這些欄位
            //carNumberView.setVisibility(View.GONE);
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

        getActionBar().setTitle(current_location);

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
                        , Double.valueOf(strToLng)), 15);
        map.animateCamera(center);


        //Current
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        markerOpt.title("現在位置");
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        map.addMarker(markerOpt).showInfoWindow();

        //Marker
        MarkerOptions markerOpt2 = new MarkerOptions();
        markerOpt2.position(new LatLng(Double.valueOf(strToLat)
                , Double.valueOf(strToLng)));
        markerOpt2.title(address);
        markerOpt2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        map.addMarker(markerOpt2).showInfoWindow();

        //Draw Line
        PolylineOptions polylineOpt = new PolylineOptions();
        polylineOpt.add(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        polylineOpt.add(new LatLng(Double.valueOf(strToLat)
                , Double.valueOf(strToLng)));

        polylineOpt.color(Color.BLUE);

        Polyline polyline = map.addPolyline(polylineOpt);
        polyline.setWidth(10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_navi:
                goBrowser();
                return true;
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
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
