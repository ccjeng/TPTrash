
package com.oddsoft.tpetrash2.model.TPCloth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result_ {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("行政區")
    @Expose
    private String region;
    @SerializedName("團體名稱")
    @Expose
    private String branch;
    @SerializedName("臺北市核准地點")
    @Expose
    private String address;
    @SerializedName("備註")
    @Expose
    private String memo;
    @SerializedName("經度")
    @Expose
    private String lng;
    @SerializedName("緯度")
    @Expose
    private String lat;
    @SerializedName("\ufeff核准編號")
    @Expose
    private String no;
    @SerializedName("電話")
    @Expose
    private String tel;

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
     *     The region
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

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
