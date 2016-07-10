package com.oddsoft.tpetrash2.controller;

import com.oddsoft.tpetrash2.model.TPFix.TPFix;
import com.oddsoft.tpetrash2.model.TPFood.TPFood;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andycheng on 2016/6/30.
 */
public interface TaipeiOpenDataService {

    @GET("apiAccess?scope=resourceAquire&rid=8077ae33-7a3a-462e-8392-0331f795bb09")
    Observable<TPFix> getTaipeiFixLocation();

    @GET("apiAccess?scope=resourceAquire&rid=6eeb9b45-36ab-48bf-9625-ac04230d1e7a")
    Observable<TPFood> getTaipeiFoodLocation();

}
