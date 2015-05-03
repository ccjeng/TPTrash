package com.oddsoft.tpetrash2;

import com.google.android.gms.ads.*;
import com.oddsoft.tpetrash2.adapter.JSONResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

public class MainActivity extends ListActivity {

    private static final String TAG = "MainActivity";
    private static boolean getLocationStatus = false;
    private static boolean getNetworkStatus = false;
    private static Double longitude;
    private static Double latitude;
    private static int rownum;
    private static int distance;
    private static int hour;
    private static String sorting;

    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private MyLocationListener mylistener;
    private Context context;
    private ArrayList<TrashItem> result;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayHomeAsUpEnabled(false);

        // Check Network Status
        if (!isNetworkAvailable()) {
            getNetworkStatus = false;
            showNetworkError();
        } else {
            getNetworkStatus = true;
        }

        // Check GPS Status
        locationServiceInitial();

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 16)
            hour = 16;

        getPref();

        if (getNetworkStatus && getLocationStatus) {
            new HttpGetTask(MainActivity.this).execute();
        }
        adView();
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
/*        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // 仿真器
                .addTestDevice("7710C21FF2537758BF3F80963477D68E") // 我的 Galaxy Nexus 測試手機
                .build();*/
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Prefs.class));
                return true;
            case R.id.exit_settings:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String distancePreference = prefs.getString("distance", "5");
        String rownumPreference = prefs.getString("rownum", "5");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        rownum = Integer.valueOf(rownumPreference);
        sorting = String.valueOf(sortingPreference);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        goBrowser(result.get(position).getLocation().toString());

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
    }
    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }


    private class HttpGetTask extends AsyncTask<Void, Void, ArrayList<TrashItem>> {
        private ProgressDialog dialog;

        private ListActivity activity;

        // private List<Message> messages;
        public HttpGetTask(ListActivity activity) {
            this.activity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        private Context context;

        // Build RESTful trash API
        private String URL = "http://tptrash-api.herokuapp.com/" + +hour + "/"
                + rownum + "/" + distance + "/" + longitude + "/" + latitude;

        AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("處理中");
            this.dialog.show();
        }


        @Override
        protected ArrayList<TrashItem> doInBackground(Void... params) {
            Log.d(TAG, URL);
            HttpGet request = new HttpGet(URL);
            JSONResponseHandler responseHandler = new JSONResponseHandler();
            try {
                return mClient.execute(request, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<TrashItem> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (null != mClient)
                mClient.close();
            setListAdapter(new ArrayAdapter<TrashItem>(
                    MainActivity.this,
                    R.layout.listitem, result));

            if (getListView().getAdapter().getCount() == 0) {
                Toast.makeText(MainActivity.this, R.string.data_not_found, Toast.LENGTH_LONG)
                        .show();
            }
            sorting();

        }
    }

    private void sorting() {
        if (sorting.equals("TIME")) { //check time sorting
            if (result != null) {
                Collections.sort(result, new Comparator<TrashItem>() {
                    @Override
                    public int compare(TrashItem item1, TrashItem item2) {
                        return item1.getStartTime().compareTo(item2.getStartTime());
                    }
                });
            }
        }
    }

    private void goBrowser(String toLocation) {
        String from = "saddr=" + latitude + "," + longitude;
        String to = "daddr=" + toLocation.toString();
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

    }

    private boolean isNetworkAvailable() {
        final ConnectivityManager connMgr = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable()) {
            return true;
        } else if (mobile.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    private void showNetworkError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.network_error)
                .setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                                // empty
                            }
                        }).show();
    }

    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE); // ���o�t�Ωw���A��
        // Define the criteria how to select the location provider
        Criteria criteria = new Criteria(); // ���T���Ѫ̿����з�
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // default
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // get the best provider depending on the criteria
        bestProvider = lms.getBestProvider(criteria, true); // ���ܺ��ǫ׳̰������Ѫ�
        Location location = lms.getLastKnownLocation(bestProvider);

        mylistener = new MyLocationListener();

        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        // location updates: at least 5 meter and 1000 minsecs change
        lms.requestLocationUpdates(bestProvider, 1000, 5, mylistener);

    }

private class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        // Initialize the location fields
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        getLocationStatus = true;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Toast.makeText(MainActivity.this,
        // provider + "'s status changed to " + status + "!",
        // Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Toast.makeText(MainActivity.this,
        // "Provider " + provider + " enabled!", Toast.LENGTH_SHORT)
        // .show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        // Toast.makeText(MainActivity.this,
        // "Provider " + provider + " disabled!", Toast.LENGTH_SHORT)
        // .show();
    }
}
}
