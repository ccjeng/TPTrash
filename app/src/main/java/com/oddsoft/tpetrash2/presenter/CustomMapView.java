package com.oddsoft.tpetrash2.presenter;

import com.oddsoft.tpetrash2.utils.Utils;

/**
 * Created by andycheng on 2016/10/3.
 */

public interface CustomMapView {

    void initView();
    void showError(String message, Utils.Mode mode);

}
