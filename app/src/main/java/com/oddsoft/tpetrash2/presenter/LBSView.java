package com.oddsoft.tpetrash2.presenter;

import android.location.Location;

import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;

import java.util.List;

/**
 * Created by andycheng on 2016/9/23.
 */

public interface LBSView {

    void initView();

    void spinnerSetSelection();
    void spinnerSelected();
    void setRecyclerView(List<ArrayItem> items, Location location);
    void showError(String message, Utils.Mode mode);

}
