package com.oddsoft.tpetrash2.view.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.MapFragment;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.presenter.InfoPresenter;
import com.oddsoft.tpetrash2.presenter.InfoView;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Constant;
import com.oddsoft.tpetrash2.view.base.Application;
import com.oddsoft.tpetrash2.view.base.MVPBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class InfoActivity extends MVPBaseActivity<InfoView, InfoPresenter> implements InfoView {

    private static final String TAG = InfoActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Analytics ga;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);
        mPresenter.onCreate();

    }

    @Override
    public void initView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //set toolbar title
        getSupportActionBar().setTitle(mPresenter.time);

        // Set up the map fragment
        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment));
        mapFragment.getMapAsync(mPresenter);

        adView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_navi:
                mPresenter.goBrowser();
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
        super.onDestroy();

        if (adView != null)
            adView.destroy();
    }

    private void adView() {

        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.footerLayout);

        adView = new AdView(this);
        adView.setAdUnitId(Constant.ADMOB_UNIT_ID_INFO);
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
    protected InfoPresenter createPresenter() {
        return new InfoPresenter(this, this);
    }
}
