package com.oddsoft.tpetrash2.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.presenter.base.BasePresenter;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;

import java.util.List;

/**
 * Created by andycheng on 2016/9/24.
 */

public class QueryPresenter extends BasePresenter<QueryView> {

    private static final String TAG = QueryPresenter.class.getSimpleName();

    private QueryView view;
    private Context context;

    public int distance;
    public String sorting;
    public boolean isQueryRunning;

    public QueryPresenter(QueryView view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void spinnerSelected(){
        view.spinnerSelected();
    }

    public void runQuery(final int day, final int hour, final String region) {
        String strHour = String.valueOf(hour);
        String weekTag = Utils.getWeekTag(day);
        String strRegion = region.substring(3, region.length());

        Log.d(TAG, day + " - " + hour + " - " + strRegion);


        AVQuery<ArrayItem> query = AVQuery.getQuery(ArrayItem.class);

        query.orderByAscending("time");

        query.whereEqualTo(weekTag, "Y")
                .whereEqualTo("region", strRegion)
                .whereEqualTo("hour", strHour);


        isQueryRunning = true;
        query.findInBackground(new FindCallback<ArrayItem>() {
            public void done(List<ArrayItem> avObjects, AVException e) {

                if (e == null) {
                    view.setRecyclerView(avObjects);

                } else {
                    view.showError(context.getString(R.string.network_error), Utils.Mode.ERROR);
                }

                isQueryRunning = false;

            }
        });

    }

    @Override
    public void onCreate() {
        getPref();
        view.initView();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {
        getPref();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String distancePreference = prefs.getString("distance", "1");
        String sortingPreference = prefs.getString("sorting", "DIST");

        distance = Integer.valueOf(distancePreference);
        if (distance > 10) {
            distance = 10;
        }
        sorting = String.valueOf(sortingPreference);
    }

}
