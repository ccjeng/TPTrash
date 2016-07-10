package com.oddsoft.tpetrash2.controller;

import com.oddsoft.tpetrash2.model.NPRecycle;
import com.oddsoft.tpetrash2.model.RealtimeCar;

import java.util.ArrayList;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andycheng on 2016/6/27.
 */
public interface NewTaipeiOpenDataService {

    @GET("api/28AB4122-60E1-4065-98E5-ABCCB69AACA6?$format=json")
    Observable<ArrayList<RealtimeCar>> getRealTimeCar();

    @GET("api/7E97E0D4-18EA-4BCD-8567-D85DC861CA40?$format=json")
    Observable<ArrayList<NPRecycle>> getNewTaipeiRecycleLocation();
}
