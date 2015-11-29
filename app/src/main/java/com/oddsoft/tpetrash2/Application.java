package com.oddsoft.tpetrash2;

/**
 * Created by andycheng on 2015/5/5.
 */

import android.location.Location;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.HashMap;


public class Application extends android.app.Application {
    // Debugging switch 
    public static final boolean APPDEBUG = true;

    // Debugging tag for the application
    public static final String APPTAG = Application.class.getSimpleName();

    //Admob
    public static final String ADMOB_TEST_DEVICE_ID = "DF9E888CAA233DE54A7FD15B3B1A1522";
    public static final String ADMOB_UNIT_ID = "ca-app-pub-6914084100751028/1776974015";
    //Vpon
    public static final String VPON_UNIT_ID = "8a8081824fb5a83f014fc9a9ba071724";

    //Parse
    private static final String PARSE_APPLICATION_ID = "nxkxfDhpFQBXOReTPFIPhGIaYowmT5uuscj3w3Kb";
    private static final String PARSE_CLIENT_KEY = "oo7CwnSrT3XCjVHuN3r1JBw7rvJzjmYZCRCX9e2U";

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ArrayItem.class);

        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
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
