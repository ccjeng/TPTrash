package com.oddsoft.tpetrash2.view.activity;

import android.content.Intent;
import android.location.Location;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.presenter.LBSPresenter;
import com.oddsoft.tpetrash2.presenter.LBSView;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;
import com.oddsoft.tpetrash2.view.adapter.ArrayItemAdapter;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.MVPBaseActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LBSActivity extends MVPBaseActivity<LBSView, LBSPresenter> implements LBSView  {

    private static final String TAG = LBSActivity.class.getSimpleName();

    private static int hour;
    private static int currentHour;

    @Bind(R.id.day_spinner)
    Spinner daySpinner;
    @Bind(R.id.hour_spinner)
    Spinner hourSpinner;
    @Bind(R.id.sort_spinner)
    Spinner sortSpinner;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorlayout;

    private AdView adView;

    private String[] dayCode;
    private String[] dayName;
    private String[] hourCode;
    private String[] hourName;
    private String[] sortCode;
    private String[] sortName;
    private int today;
    private String selectedDay;
    private String selectedHour;
    private String selectedSort;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    private Analytics ga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbs);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        mPresenter.onCreate();
    }

    @Override
    public void initView() {

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

        adView();
    }

    private void initSpinner() {
        dayCode = getResources().getStringArray(R.array.day_spinnner_code);
        dayName = getResources().getStringArray(R.array.day_spinnner_name);
        hourCode = getResources().getStringArray(R.array.hour_spinnner_code);
        hourName = getResources().getStringArray(R.array.hour_spinnner_name);
        sortCode = getResources().getStringArray(R.array.pref_sorting_code);
        sortName = getResources().getStringArray(R.array.pref_sorting_item);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this, R.array.day_spinnner_name,
                android.R.layout.simple_spinner_item);

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mPresenter.spinnerSelected();
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
                mPresenter.spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.pref_sorting_item,
                android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(Arrays.asList(sortCode).indexOf(mPresenter.sorting));
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mPresenter.spinnerSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    @Override
    protected LBSPresenter createPresenter() {
        return new LBSPresenter(this, this);
    }

    @Override
    public void spinnerSelected() {
        selectedDay = dayCode[daySpinner.getSelectedItemPosition()];
        selectedHour = hourCode[hourSpinner.getSelectedItemPosition()];
        selectedSort = sortCode[sortSpinner.getSelectedItemPosition()];

        if (hourSpinner.getSelectedItemPosition()!=0 &&
                !mPresenter.isQueryRunning()) {

            progressWheel.setVisibility(View.VISIBLE);

            mPresenter.runQuery(Integer.valueOf(selectedDay)
                    , Integer.valueOf(selectedHour)
                    , selectedSort);

            //show 3/7 messages
            if (selectedDay.equals("3") || selectedDay.equals("0")) {
                Toast.makeText(this, Time.getDayOfWeekName(Integer.valueOf(selectedDay))+"，台北市沒有收垃圾，新北市僅部分區域有收垃圾！"
                        ,Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void spinnerSetSelection() {
        if (Application.getRefreshFlag()) {
            daySpinner.setSelection(Arrays.asList(dayCode).indexOf(String.valueOf(today)));
            hourSpinner.setSelection(Arrays.asList(hourCode).indexOf(String.valueOf(hour)));
            Application.setRefreshFlag(false);
        }
    }

    @Override
    public void setRecyclerView(List<ArrayItem> items, final Location location) {

        progressWheel.setVisibility(View.GONE);

        ArrayItemAdapter adapter = new ArrayItemAdapter(this, items, selectedDay, currentHour, location);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ArrayItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ArrayItem item) {
                Log.d(TAG, item.getAddress());
                goIntent(item, location);
            }
        });

        if (items.size() == 0) {
            this.showError(getString(R.string.data_not_found), Utils.Mode.INFO);
        }


    }

    private void adView() {

        LinearLayout adBannerLayout = (LinearLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_LBS);
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
    public void onStop() {
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();

        mPresenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adView != null)
            adView.resume();

        mPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        mPresenter.onDestroy();
        super.onDestroy();
    }


    private void goIntent(ArrayItem item, Location location) {

        ga.trackEvent(this, "Location", "Region", item.getRegion(), 0);
        ga.trackEvent(this, "Location", "Address", item.getFullAddress(), 0);

        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("item", (Parcelable) item);

        Bundle bundle = new Bundle();
        bundle.putString("fromLat", String.valueOf(location.getLatitude()));
        bundle.putString("fromLng", String.valueOf(location.getLongitude()));
        bundle.putString("selectedDay", selectedDay);
        intent.putExtras(bundle);

        startActivity(intent);

    }

    @Override
    public void showError(String message, Utils.Mode mode) {
        Utils.showSnackBar(coordinatorlayout, message, mode);
    }
}
