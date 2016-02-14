package com.oddsoft.tpetrash2.utils;

/**
 * Created by andycheng on 2015/8/19.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

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


    public static String getWeekTag(){
        String tag = "";

        switch (Time.getDayOfWeekNumber()) {
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

}
