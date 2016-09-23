package com.oddsoft.tpetrash2.view.base;

/**
 * Created by andycheng on 2015/5/5.
 */

import android.location.Location;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.oddsoft.tpetrash2.BuildConfig;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.utils.Constant;

import java.util.HashMap;


public class Application extends android.app.Application {
    // Debugging switch 
    public static final boolean APPDEBUG = BuildConfig.DEBUG;

    // Debugging tag for the application
    public static final String APPTAG = Application.class.getSimpleName();

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AVOSCloud.initialize(this, Constant.LEANCLOUD_APP_ID, Constant.LEANCLOUD_APP_KEY);
        AVOSCloud.useAVCloudCN();
        AVObject.registerSubclass(ArrayItem.class);

    }


    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-19743390-13";
    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            if (APPDEBUG) {
                analytics.getInstance(this).setDryRun(true);
            }
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    //Global variable for current location
    private static Location mLocation;
    public static Location getCurrentLocation(){
        if (mLocation == null) {
            mLocation = new Location("");
            mLocation.setLatitude(24.8979347);
            mLocation.setLongitude(121.5393508);
        }
        return mLocation;
    }
    public static void setCurrentLocation(Location l){
        mLocation = l;
    }

    private static Boolean mRefresh = true;
    public static Boolean getRefreshFlag(){
        return mRefresh;
    }
    public static void setRefreshFlag(Boolean s){
        mRefresh = s;
    }
}
