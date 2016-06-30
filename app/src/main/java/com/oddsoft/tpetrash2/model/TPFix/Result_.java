
package com.oddsoft.tpetrash2.model.TPFix;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result_ {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("\ufeffRegion")
    @Expose
    private String region;
    @SerializedName("Branch")
    @Expose
    private String branch;
    @SerializedName("Phone")
    @Expose
    private String phone;
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("Memo")
    @Expose
    private String memo;
    @SerializedName("Lng")
    @Expose
    private String lng;
    @SerializedName("Lat")
    @Expose
    private String lat;

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The _id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The region
     */
    public String getRegion() {
        return region;
    }

    /**
     * 
     * @param region
     *     The ﻿Region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 
     * @return
     *     The branch
     */
    public String getBranch() {
        return branch;
    }

    /**
     * 
     * @param branch
     *     The Branch
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * 
     * @return
     *     The phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 
     * @param phone
     *     The Phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 
     * @return
     *     The address
     */
    public String getAddress() {
        return address;
    }

    /**
     * 
     * @param address
     *     The Address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 
     * @return
     *     The memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * 
     * @param memo
     *     The Memo
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 
     * @return
     *     The lng
     */
    public String getLng() {
        return lng;
    }

    /**
     * 
     * @param lng
     *     The Lng
     */
    public void setLng(String lng) {
        this.lng = lng;
    }

    /**
     * 
     * @return
     *     The lat
     */
    public String getLat() {
        return lat;
    }

    /**
     * 
     * @param lat
     *     The Lat
     */
    public void setLat(String lat) {
        this.lat = lat;
    }

}
