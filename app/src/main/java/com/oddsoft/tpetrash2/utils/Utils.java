package com.oddsoft.tpetrash2.utils;

/**
 * Created by andycheng on 2015/8/19.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.avos.avoscloud.AVGeoPoint;

public class Utils {
    public static final String TAG = "Utils";
    private static String mLastVersionRun;
    private static SharedPreferences prefs = null;

    public static void setLastVersionRun(String lastVersionRun) {
        put("LastVersionRun", lastVersionRun);
        Utils.mLastVersionRun = lastVersionRun;
    }

    public static String getLastVersionRun() {
        return mLastVersionRun;
    }

    public static boolean newVersionInstalled(Activity a) {
        if (prefs==null)
            prefs = PreferenceManager.getDefaultSharedPreferences(a);
        String thisVersion = getVersion(a);
        Utils.mLastVersionRun = prefs.getString("LastVersionRun", "");
        String lastVersionRun = mLastVersionRun;

        setLastVersionRun(thisVersion);
        if (thisVersion.equals(lastVersionRun)) {
            return false;
        } else {
            return true;
        }
    }


    public static boolean isNewInstallation(Activity a) {
        if (prefs==null)
            prefs = PreferenceManager.getDefaultSharedPreferences(a);
        if (prefs.getString("LastVersionRun", "").equals("")) {
            setLastVersionRun(getVersion(a));
            return true;
        } else
            return false;
    }
    /**
     * Retrieves the packaged version of the application
     *
     * @param a
     *            - The Activity to retrieve the current version
     * @return the version-string
     */
    public static String getVersion(Activity a) {
        String result = "";
        try {
            PackageManager manager = a.getPackageManager();
            PackageInfo info = manager.getPackageInfo(a.getPackageName(), 0);
            result = info.versionName;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Unable to get application version: " + e.getMessage());
            result = "";
        }
        return result;
    }

    private static void put(String constant, Object o) {
        SharedPreferences.Editor editor = prefs.edit();
        if (o instanceof String) {
            editor.putString(constant, (String) o);
        } else if (o instanceof Integer) {
            editor.putInt(constant, (Integer) o);
        } else if (o instanceof Long) {
            editor.putLong(constant, (Long) o);
        } else if (o instanceof Boolean) {
            editor.putBoolean(constant, (Boolean) o);
        }
        editor.apply();
    }

    public static String getWeekFoodTag(){
        String tag = "";

        switch (Time.getDayOfWeekNumber()) {
            case "1":
                tag = "foodscraps_mon";
                break;
            case "2":
                tag = "foodscraps_tue";
                break;
            case "3":
                tag = "foodscraps_wed";
                break;
            case "4":
                tag = "foodscraps_thu";
                break;
            case "5":
                tag = "foodscraps_fri";
                break;
            case "6":
                tag = "foodscraps_sat";
                break;
            case "0":
                tag = "foodscraps_sun";
                break;
        }

        return tag;
    }

    public static String getWeekGarbageTag(){
        String tag = "";

        switch (Time.getDayOfWeekNumber()) {
            case "1":
                tag = "garbage_mon";
                break;
            case "2":
                tag = "garbage_tue";
                break;
            case "3":
                tag = "garbage_wed";
                break;
            case "4":
                tag = "garbage_thu";
                break;
            case "5":
                tag = "garbage_fri";
                break;
            case "6":
                tag = "garbage_sat";
                break;
            case "0":
                tag = "garbage_sun";
                break;
        }

        return tag;
    }

    public static String getWeekRecyclingTag(){
        String tag = "";

        switch (Time.getDayOfWeekNumber()) {
            case "1":
                tag = "recycling_mon";
                break;
            case "2":
                tag = "recycling_tue";
                break;
            case "3":
                tag = "recycling_wed";
                break;
            case "4":
                tag = "recycling_thu";
                break;
            case "5":
                tag = "recycling_fri";
                break;
            case "6":
                tag = "recycling_sat";
                break;
            case "0":
                tag = "recycling_sun";
                break;
        }

        return tag;
    }


    public static String getWeekTag(int today){
        String tag = "";

        switch (String.valueOf(today)) {
            case "1":
                tag = "mon";
                break;
            case "2":
                tag = "tue";
                break;
            case "3":
                tag = "wed";
                break;
            case "4":
                tag = "thu";
                break;
            case "5":
                tag = "fri";
                break;
            case "6":
                tag = "sat";
                break;
            case "0":
                tag = "sun";
                break;
        }

        return tag;
    }


    public enum Mode {
        ERROR, INFO, WARNING
    }
    public static void showSnackBar(View view, String message, Mode mode) {

        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);

        TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        switch(mode) {
            case ERROR:
                snackbar.getView().setBackgroundColor(Color.RED);
                tv.setTextColor(Color.WHITE);
                break;
            case INFO:
            case WARNING:
                snackbar.getView().setBackgroundColor(Color.GREEN);
                tv.setTextColor(Color.BLACK);
                break;
        }


        snackbar.show();

    }


    public static AVGeoPoint geoPointFromLocation(Location loc) {
        return new AVGeoPoint(loc.getLatitude(), loc.getLongitude());
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }



}
