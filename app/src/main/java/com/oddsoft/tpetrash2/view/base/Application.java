package com.oddsoft.tpetrash2.view.base;

/**
 * Created by andycheng on 2015/5/5.
 */

import android.support.multidex.MultiDexApplication;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.oddsoft.tpetrash2.BuildConfig;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.model.ArrayItem;

import java.util.HashMap;

public class Application extends MultiDexApplication {
    // Debugging switch 
    public static final boolean APPDEBUG = BuildConfig.DEBUG;
    public static final String APPTAG = Application.class.getSimpleName();

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

}
