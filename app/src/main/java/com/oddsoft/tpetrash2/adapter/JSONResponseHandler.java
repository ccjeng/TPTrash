package com.oddsoft.tpetrash2.adapter;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.oddsoft.tpetrash2.TrashItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by andycheng on 2015/5/3.
 */
public class JSONResponseHandler implements ResponseHandler<ArrayList<TrashItem>> {
    private static final String TAG = "JSONResponseHandler";
    private ArrayList<TrashItem> result;

    @Override
    public ArrayList<TrashItem> handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
        TrashItem item = null;
        String text = null;
        result = new ArrayList<TrashItem>();

        HttpEntity entity = response.getEntity();
        text = getUTF8ContentFromEntity(entity);

        try {
            String lng = "";
            String lat = "";
            String address = "";
            String carTime = "";
            String location = "";
            String name = "";
            String time = "";

            JsonFactory jsonfactory = new JsonFactory();
            JsonParser jsonParser = jsonfactory.createJsonParser(text);
            JsonToken token = jsonParser.nextToken();

            // ArrayList objectArray = new ArrayList();
            // Expected JSON is an array so if current token is "[" then
            // while
            // we don't get
            // "]" we will keep parsing

            if (token == JsonToken.START_ARRAY) {
                while (token != JsonToken.END_ARRAY) {
                    // Inside array there are many objects, so it has to
                    // start
                    // with "{" and end with "}"
                    token = jsonParser.nextToken();
                    if (token == JsonToken.START_OBJECT) {
                        while (token != JsonToken.END_OBJECT) {
                            // Each object has a name which we will use to
                            // identify the type.
                            token = jsonParser.nextToken();
                            if (token == JsonToken.FIELD_NAME) {
                                String fieldname = jsonParser
                                        .getCurrentName();
                                // Log.d(TAG, fieldname);

                                if ("Address".equals(fieldname)) {
                                    jsonParser.nextToken();
                                    address = jsonParser.getText();
                                }

                                if ("CarTime".equals(fieldname)) {
                                    jsonParser.nextToken();
                                    carTime = jsonParser.getText();
                                }

                                if ("Lng".equals(fieldname)) {
                                    jsonParser.nextToken();
                                    lng = jsonParser.getText();
                                }

                                if ("Lat".equals(fieldname)) {
                                    jsonParser.nextToken();
                                    lat = jsonParser.getText();
                                }

                            }

                        }
                        location = lat + "," + lng;
                        name = address.substring(6, address.length());
                        time = carTime;

                        Log.d(TAG, "location=" + location);
                        Log.d(TAG, "name=" + name);
                        Log.d(TAG, "time=" + time);

                        item = new TrashItem(location, time, name);
                        result.add(item);
                    }

                }

            }
            jsonParser.close();

        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }

        return result;
    }

    protected String getUTF8ContentFromEntity(HttpEntity entity)
            throws IllegalStateException, IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                entity.getContent(), "UTF-8"));
        return reader.readLine();
    }
}

