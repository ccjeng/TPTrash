package com.oddsoft.tpetrash2.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.controller.LocationService;
import com.oddsoft.tpetrash2.presenter.base.BasePresenter;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.model.ArrayItem;
import com.oddsoft.tpetrash2.view.base.Application;

import java.util.List;

/**
 * Created by andycheng on 2016/9/23.
 */

public class LBSPresenter extends BasePresenter<LBSView> implements LocationConnectedListener {

    private static final String TAG = LBSPresenter.class.getSimpleName();

    private LBSView view;
    private Context context;
    private LocationService locationService;
    public Location currentLocation;

    public int distance;
    public String sorting;

    public boolean isQueryRunning;


    public LBSPresenter(LBSView view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void runQuery(final int day, final int hour, final String sort) {

        currentLocation = locationService.getCurrentLocation();

        if (currentLocation != null) {

            if (Application.APPDEBUG)
                Log.d(TAG, "location = " + currentLocation.toString());

            String strHour = String.valueOf(hour);
            String weekTag = Utils.getWeekTag(day);

            Log.d(TAG, "weekTag = " + weekTag);
            AVGeoPoint userLocation = new AVGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

            if (sort.equals("TIME")) {
                query.orderByAscending("time");
            }

            query.whereEqualTo(weekTag, "Y")
                    .whereEqualTo("hour", strHour)
                    .whereWithinKilometers("location", userLocation, distance)
                    .setLimit(100);

            isQueryRunning = true;
            query.findInBackground(new FindCallback<ArrayItem>() {
                public void done(List<ArrayItem> avObjects, AVException e) {

                    if (e == null) {
                        view.setRecyclerView(avObjects, currentLocation);
                      //  Application.setRefreshFlag(true);

                    } else {
                        view.showError(context.getString(R.string.network_error), Utils.Mode.ERROR);
                    }
                }
            });

        } else {
            //location error
            view.showError(context.getString(R.string.location_error), Utils.Mode.ERROR);

        }
        isQueryRunning = false;
    }

    /*
 * Helper method to get the Parse GEO point representation of a location
 */
    private AVGeoPoint geoPointFromLocation(Location loc) {
        return new AVGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public void spinnerSelected() {
        view.spinnerSelected();
    }

    @Override
    public void onStart() {
        locationService.connect();
    }

    @Override
    public void onCreate() {
        getPref();
        view.initView();
        locationService = new LocationService(context);
        locationService.setLocationConnectedListener(this);

        if (Utils.isNetworkConnected(context)) {
            locationService.connect();
        } else {
            view.showError(context.getString(R.string.network_error), Utils.Mode.ERROR);
        }
    }

    /**
     * LocationConnectedListener
    * */
    @Override
    public void onLocationServiceConnected(Location location) {
        Log.d(TAG, "onLocationServiceConnected");

        currentLocation = location;

        //auto set selected selection on spinner
        if (currentLocation != null) {
            view.spinnerSetSelection();
        } else {
            //location error
            view.showError(context.getString(R.string.location_error), Utils.Mode.ERROR);
        }

    }

    @Override
    public void onPause() {
        locationService.pause();
    }


    @Override
    public void onStop() {
        locationService.disconnect();
    }

    @Override
    public void onResume() {
        getPref();
        locationService.connect();
    }

    @Override
    public void onDestroy() {

    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String distancePreference = prefs.getString("distance", "1");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        if (distance > 10) {
            distance = 10;
        }
        sorting = String.valueOf(sortingPreference);
    }


}
