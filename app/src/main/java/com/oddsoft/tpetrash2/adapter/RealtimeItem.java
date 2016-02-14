package com.oddsoft.tpetrash2.adapter;

/**
 * Created by andycheng on 2016/2/14.
 */
public class RealtimeItem {

    public String address;
    public String car;
    public Double lat;
    public Double lng;
    public String time;
    public String lineid;

    public RealtimeItem(){

    }

    public String getAddress() {
        return address;
    }
    public String getCar(){
        return car;
    }
    public String getTime(){
        return time;
    }
    public String getLineid(){
        return lineid;
    }
    public Double getLat(){
        return lat;
    }
    public Double getLng(){
        return lng;
    }

}
