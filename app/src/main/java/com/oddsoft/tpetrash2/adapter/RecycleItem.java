package com.oddsoft.tpetrash2.adapter;

import android.util.Log;

import com.oddsoft.tpetrash2.view.base.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by andycheng on 2015/7/10.
 */
public class RecycleItem {

    private String item = null;
    private String description = null;
    private String category = null;
    private String subcategory = null;
    private String time = null;

    public RecycleItem() {

    }
    public RecycleItem(String item, String description, String category, String subcategory, String time) {
        this.item = item;
        this.description = description;
        this.category = description;
        this.subcategory = description;
        this.time = description;
    }

    // Constructor to convert JSON object into a Java class instance
    public RecycleItem(JSONObject object){
        try {
            this.item = object.getString("item");
            this.description = object.getString("description");
            this.category = object.getString("category");
            this.subcategory = object.getString("subcategory");
            this.time = object.getString("time");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<RecycleItem> fromJson(JSONArray jsonObjects, CharSequence keyword) {
        ArrayList<RecycleItem> items = new ArrayList<RecycleItem>();
        if (!Application.APPDEBUG)
            Log.d(Application.APPTAG, keyword.toString());

        for (int i = 0; i < jsonObjects.length(); i++) {
            try {

                if (jsonObjects.getJSONObject(i).get("keyword").toString().contains(keyword)) {
                    if (Application.APPDEBUG)
                        Log.d(Application.APPTAG, "--" + keyword.toString());

                    items.add(new RecycleItem(jsonObjects.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return items;
    }
    public String getItem() {
        return "垃圾項目：" + item;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public String getDescription() {
        return "處理說明："+description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return "類別：" +category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSubcategory() {
        return " - " + subcategory;
    }
    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getTime() {
        return "回收時間 (限台北市): " + time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString()
    {
        return description;
    }
}
