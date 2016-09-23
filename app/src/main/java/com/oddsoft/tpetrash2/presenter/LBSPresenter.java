package com.oddsoft.tpetrash2.presenter;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.oddsoft.tpetrash2.presenter.base.BasePresenter;

/**
 * Created by andycheng on 2016/9/23.
 */

public class LBSPresenter extends BasePresenter<LBSView>  implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    public void runQuery() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
