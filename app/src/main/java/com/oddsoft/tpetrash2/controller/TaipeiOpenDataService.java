package com.oddsoft.tpetrash2.controller;

import com.oddsoft.tpetrash2.model.TPFix.TPFix;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andycheng on 2016/6/30.
 */
public interface TaipeiOpenDataService {

    @GET("apiAccess?scope=resourceAquire&rid=8077ae33-7a3a-462e-8392-0331f795bb09")
    Observable<TPFix> getTaipeiFixLocation();
}
