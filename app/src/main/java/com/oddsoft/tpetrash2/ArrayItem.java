package com.oddsoft.tpetrash2;

/**
 * Data model for a trash item.
 */

import com.oddsoft.tpetrash2.utils.Time;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;

@ParseClassName("TaipeiAll")
public class ArrayItem extends ParseObject {

    public String getAddress() {
        return getString("Address");
    }

    public String getCarNo() {
        return getString("CarNo");
    }

    public String getCarNumber() {
        return getString("CarNumber");
    }

    public String getCarTime() {
        return getString("CarTime");
    }

    public String getDepName() {
        return getString("DepName");
    }

    //里
    public String getLi() {
        return getString("Li");
    }

    //區
    public String getRegion() {
        return getString("Region");
    }

    public String getCity() {
        return getString("city");
    }

    public String getFood1() {
        return getString("foodscraps_mon");
    }

    public String getFood2() {
        return getString("foodscraps_tue");
    }

    public String getFood3() {
        return getString("foodscraps_wed");
    }

    public String getFood4() {
        return getString("foodscraps_thu");
    }

    public String getFood5() {
        return getString("foodscraps_fri");
    }

    public String getFood6() {
        return getString("foodscraps_sat");
    }

    public String getFood7() {
        return getString("foodscraps_sun");
    }

    public String getGarbage1() {
        return getString("garbage_mon");
    }

    public String getGarbage2() {
        return getString("garbage_tue");
    }

    public String getGarbage3() {
        return getString("garbage_wed");
    }

    public String getGarbage4() {
        return getString("garbage_thu");
    }

    public String getGarbage5() {
        return getString("garbage_fri");
    }

    public String getGarbage6() {
        return getString("garbage_sat");
    }

    public String getGarbage7() {
        return getString("garbage_sun");
    }

    public String getRecycling1() {
        return getString("recycling_mon");
    }

    public String getRecycling2() {
        return getString("recycling_tue");
    }

    public String getRecycling3() {
        return getString("recycling_wed");
    }

    public String getRecycling4() {
        return getString("recycling_thu");
    }

    public String getRecycling5() {
        return getString("recycling_fri");
    }

    public String getRecycling6() {
        return getString("recycling_sat");
    }

    public String getRecycling7() {
        return getString("recycling_sun");
    }

    private void checkAvailable(String type) {
        Time t = new Time();
        switch (t.getDayOfWeekNumber()) {
            case "1":

                break;
        }

    }

    public String getMemo() {
        return getString("memo");
    }

    public String getRank() {
        return getString("rank");
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public String getDistance(ParseGeoPoint current) {

        Double distance = getLocation().distanceInKilometersTo(current);
        String strDistance = "";
        DecimalFormat df;
        String unit = "";

        if (distance < 1) {
            distance = distance * 1000;
            unit = " 公尺";
        } else {
            unit = " 公里";
        }
        df = new DecimalFormat("#");
        strDistance = df.format(distance);

        return strDistance + unit;
    }
    public static ParseQuery<ArrayItem> getQuery() {
        return ParseQuery.getQuery(ArrayItem.class);
    }
}
