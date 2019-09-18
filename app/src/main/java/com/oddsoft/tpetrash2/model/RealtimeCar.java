package com.oddsoft.tpetrash2.model;

import java.util.HashMap;
import java.util.Map;

public class RealtimeCar {

    private String lineid;
    private String car;
    private String time;
    private String location;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The lineid
     */
    public String getLineid() {
        return lineid;
    }

    /**
     *
     * @param lineid
     * The lineid
     */
    public void setLineid(String lineid) {
        this.lineid = lineid;
    }

    /**
     *
     * @return
     * The car
     */
    public String getCar() {
        return car;
    }

    /**
     *
     * @param car
     * The car
     */
    public void setCar(String car) {
        this.car = car;
    }

    /**
     *
     * @return
     * The time
     */
    public String getTime() {
        return time;
    }

    /**
     *
     * @param time
     * The time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     *
     * @return
     * The location
     */
    public String getLocation() {
        return location;
    }

    /**
     *
     * @param location
     * The location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}