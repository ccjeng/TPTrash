package com.oddsoft.tpetrash2;

/**
 * Created by andycheng on 2015/5/5.
 */
import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseObject;


public class Application extends android.app.Application {
    // Debugging switch
    public static final boolean APPDEBUG = true;

    // Debugging tag for the application
    public static final String APPTAG = "TPTrash";

    private static SharedPreferences preferences;

    private static final String PARSE_APPLICATION_ID = "nxkxfDhpFQBXOReTPFIPhGIaYowmT5uuscj3w3Kb";
    private static final String PARSE_CLIENT_KEY = "oo7CwnSrT3XCjVHuN3r1JBw7rvJzjmYZCRCX9e2U";


    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ArrayItem.class);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        preferences = getSharedPreferences("com.oddsoft.tpetrash2", Context.MODE_PRIVATE);

    }


    public static String getLimitRowNumber() {
        return preferences.getString("rownum", "5");
    }

    public static String getSearchDistance() {
        return preferences.getString("distance", "3");
    }

    public static String getSortingType() {
        return preferences.getString("sorting", "DIST");
    }

}
