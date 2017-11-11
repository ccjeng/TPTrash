package com.oddsoft.tpetrash2.view.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.presenter.CustomMapPresenter;
import com.oddsoft.tpetrash2.presenter.CustomMapView;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.MVPBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomMapActivity extends MVPBaseActivity<CustomMapView, CustomMapPresenter> implements CustomMapView {

    private static final String TAG = CustomMapActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinatorlayout)
    RelativeLayout coordinatorlayout;

    //MapFragment mapFragment;

    private Analytics ga;
    private AdView adView;
    private String mapType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpfix);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        Bundle bundle = this.getIntent().getExtras();
        mapType = bundle.getString("mapType");

        mPresenter.setMapType(mapType);
        mPresenter.onCreate();

    }

    private void initActionBar() {
        setSupportActionBar(toolbar);

        String title = "";
        switch (mapType) {
            case "tpfix":  //台北市資源回收及廚餘限時收受點
                title = getString(R.string.tpfix);
                break;
            case "tpfood": //台北市週三、週日廚餘專用限時收受點
                title = getString(R.string.tpfood);
                break;
            case "tpcloth": //台北市舊衣回收箱
                title = getString(R.string.tpcloth);
                break;
            case "ntrecycle": //新北市黃金資收站設置資訊
                title = getString(R.string.ntrecycle);
                break;

         }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    private void adView() {

        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_MAIN);
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
        mPresenter.onPause();
        if (adView != null)
            adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
        if (adView != null)
            adView.resume();
    }

    @Override
    public void onStop() {
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        if (adView != null)
            adView.destroy();
    }

    @Override
    public void initView() {
        initActionBar();
        adView();
    }

    @Override
    public void showError(String message, Utils.Mode mode) {
        Utils.showSnackBar(coordinatorlayout, message, mode);
    }

    @Override
    protected CustomMapPresenter createPresenter() {
        return new CustomMapPresenter(this, this);
    }

}

