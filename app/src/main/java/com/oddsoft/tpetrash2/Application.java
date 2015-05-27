package com.oddsoft.tpetrash2;

/**
 * Created by andycheng on 2015/5/5.
 */

import com.oddsoft.tpetrash2.utils.Time;
import com.parse.Parse;
import com.parse.ParseObject;


public class Application extends android.app.Application {
    // Debugging switch
    public static final boolean APPDEBUG = false;

    // Debugging tag for the application
    public static final String APPTAG = "TPTrash";

    //Admob
    public static final String ADMOB_TEST_DEVICE_ID = "7710C21FF2537758BF3F80963477D68E";

    private static final String PARSE_APPLICATION_ID = "nxkxfDhpFQBXOReTPFIPhGIaYowmT5uuscj3w3Kb";
    private static final String PARSE_CLIENT_KEY = "oo7CwnSrT3XCjVHuN3r1JBw7rvJzjmYZCRCX9e2U";


    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ArrayItem.class);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

    }

}
