package com.oddsoft.tpetrash2.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.view.adapter.ArrayItemAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.BaseActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class QueryActivity extends BaseActivity {

    private static final String TAG = QueryActivity.class.getSimpleName();
    public static final String INTENT_EXTRA_ITEM = "intentItem";

    private static int hour;
    private static int currentHour;

    @Bind(R.id.day_spinner)
    Spinner daySpinner;
    @Bind(R.id.hour_spinner)
    Spinner hourSpinner;
    @Bind(R.id.region_spinner)
    Spinner regionSpinner;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorlayout;

    private AdView adView;

    private String[] dayCode;
    private String[] dayName;
    private String[] hourCode;
    private String[] hourName;
    private String[] regionName;

    private int today;
    private String selectedDay;
    private String selectedHour;
    private String selectedRegion;
    private boolean queryRunnung;


    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;


    private Analytics ga;

    private ArrayItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        initActionBar();
        initSpinner();

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        today = Integer.valueOf(Time.getDayOfWeekNumber());

        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        hour = currentHour;

        //set hour spinner to current hour
        if (hour < 5) {
            hour = 5;
        }

        daySpinner.setSelection(Arrays.asList(dayCode).indexOf(String.valueOf(today)));
        hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));


        if (!Utils.isNetworkConnected(this)) {
            Utils.showSnackBar(coordinatorlayout, getString(R.string.network_error), Utils.Mode.ERROR);
        }

        adView();

    }

    private void initSpinner() {
        dayCode = getResources().getStringArray(R.array.day_spinnner_code);
        dayName = getResources().getStringArray(R.array.day_spinnner_name);
        hourCode = getResources().getStringArray(R.array.hour_spinnner_code);
        hourName = getResources().getStringArray(R.array.hour_spinnner_name);
        regionName = getResources().getStringArray(R.array.region_spinnner_name);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this, R.array.day_spinnner_name,
                android.R.layout.simple_spinner_item);

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
                this, R.array.hour_spinnner_name,
                android.R.layout.simple_spinner_item);

        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.region_spinnner_name,
                android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(sortAdapter);
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }


    private void spinnerSelected() {
        selectedDay = dayCode[daySpinner.getSelectedItemPosition()];
        selectedHour = hourCode[hourSpinner.getSelectedItemPosition()];
        selectedRegion = regionName[regionSpinner.getSelectedItemPosition()];

        if (regionSpinner.getSelectedItemPosition()!=0 &&
                hourSpinner.getSelectedItemPosition() != 0 &&
                !queryRunnung) {
            runQuery(Integer.valueOf(selectedDay)
                    , Integer.valueOf(selectedHour)
                    , selectedRegion);

            //show 3/7 messages
            if (selectedDay.equals("3") || selectedDay.equals("0")) {
                Toast.makeText(this, Time.getDayOfWeekName(Integer.valueOf(selectedDay))+"，台北市沒有收垃圾，新北市僅部分區域有收垃圾！"
                        ,Toast.LENGTH_LONG).show();
            }
        }


    }
    private void adView() {

        LinearLayout adBannerLayout = (LinearLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_QUERY);
        adView.setAdSize(AdSize.SMART_BANNER);
        adBannerLayout.addView(adView);

        AdRequest adRequest;

        if (Application.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Constant.ADMOB_TEST_DEVICE_ID)
                    .build();
        } else {

            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);

    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void runQuery(final int day, final int hour, final String region) {


        String strHour = String.valueOf(hour);
        String weekTag = Utils.getWeekTag(day);
        String strRegion = region.substring(3, region.length());

        Log.d(TAG, day + " - " + hour + " - " + strRegion);


        AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

        query.orderByAscending("time");

        query.whereEqualTo(weekTag, "Y")
                .whereEqualTo("region", strRegion)
                .whereEqualTo("hour", strHour);

        progressWheel.setVisibility(View.VISIBLE);

        queryRunnung = true;
        query.findInBackground(new FindCallback<ArrayItem>() {
            public void done(List<ArrayItem> avObjects, AVException e) {

                progressWheel.setVisibility(View.GONE);

                if (e == null) {

                    adapter = new ArrayItemAdapter(QueryActivity.this, avObjects, String.valueOf(day), currentHour, null);

                    recyclerView.setAdapter(adapter);

                    adapter.setOnItemClickListener(new ArrayItemAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(ArrayItem item) {
                            Log.d(TAG, item.getAddress());
                            goIntent(item);
                        }
                    });

                    if (avObjects.size() == 0) {
                        Utils.showSnackBar(coordinatorlayout, getString(R.string.data_not_found), Utils.Mode.INFO);
                    }

                } else {
                    Utils.showSnackBar(coordinatorlayout, getString(R.string.network_error), Utils.Mode.ERROR);
                }

                queryRunnung = false;

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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


    private void goIntent(ArrayItem item) {

        ga.trackEvent(this, "Location", "Region", item.getRegion(), 0);
        ga.trackEvent(this, "Location", "Address", item.getFullAddress(), 0);

        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("item", (Parcelable) item);

        Bundle bundle = new Bundle();
        bundle.putString("fromLat", "");
        bundle.putString("fromLng", "");
        bundle.putString("selectedDay", selectedDay);
        intent.putExtras(bundle);

        startActivity(intent);

    }


}
