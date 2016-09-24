package com.oddsoft.tpetrash2.presenter;

import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.ArrayItem;

import java.util.List;

/**
 * Created by andycheng on 2016/9/24.
 */

public interface QueryView {

    void initView();
    void spinnerSelected();
    void setRecyclerView(List<ArrayItem> items);
    void showError(String message, Utils.Mode mode);
}
