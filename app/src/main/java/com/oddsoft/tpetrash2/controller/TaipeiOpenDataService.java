package com.oddsoft.tpetrash2.controller;

import com.oddsoft.tpetrash2.model.TPFix.TPFix;
import com.oddsoft.tpetrash2.model.TPFood.TPFood;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andycheng on 2016/6/30.
 */
public interface TaipeiOpenDataService {

    @GET("raw/TaipeiRecycle.json")
    Observable<TPFix> getTaipeiFixLocation();

    @GET("raw/TaipeiFood.json")
    Observable<TPFood> getTaipeiFoodLocation();

}
