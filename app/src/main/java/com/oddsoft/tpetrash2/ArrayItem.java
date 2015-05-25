package com.oddsoft.tpetrash2;

/**
 * Data model for a trash item.
 */


/*
* 台北市
* 週日、三: 停收垃圾及資源回收物(廚餘)
* 週一、五: 平面類：紙類 舊衣類 乾淨塑膠袋
* 週二、四、六：乾淨保麗龍, 一般類（瓶罐、容器、小家電等.
*
* */
import com.oddsoft.tpetrash2.utils.Time;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;

@ParseClassName("TaipeiAll")
public class ArrayItem extends ParseObject {

    public String getAddress() {

        String address = getString("Address");

        if (getCity() == "Taipei") {
            return "[" + getRegion() + "] " + address.substring(6, address.length());
        } else {
            return "[" + getRegion() + "] " + address;
        }

    }

    public String getFullAddress() {
        return  getString("Address");
    }

    public String getCarNo() {
        if (getString("CarNo") != null) {
            return getString("CarNo");
        } else {
            return "";
        }
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

        String city = getString("city");

        if (city != null) {
            return "NewTaipei";
        } else {
            return "Taipei";
        }

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

    //判斷今天要不要收廚餘
    public Boolean checkTodayAvailableFood() {
        Time t = new Time();
        Boolean result = false;

        switch (t.getDayOfWeekNumber()) {
            case "1":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getFood1().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "2":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getFood2().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "3":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週三不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getFood3().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "4":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getFood4().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "5":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getFood5().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "6":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getFood6().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "7":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週日不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getFood7().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;

        }

        return result;
    }

    //判斷今天要不要收資源回收
    public Boolean checkTodayAvailableRecycling() {
        Time t = new Time();
        Boolean result = false;

        switch (t.getDayOfWeekNumber()) {
            case "1":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getRecycling1().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "2":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getRecycling2().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "3":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週三不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getRecycling3().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "4":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getRecycling4().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "5":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getRecycling5().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "6":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getRecycling6().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "7":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週日不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getRecycling7().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
        }
        return result;
    }

    //判斷今天要不要收一般垃圾
    public Boolean checkTodayAvailableGarbage() {
        Time t = new Time();
        Boolean result = false;

        switch (t.getDayOfWeekNumber()) {
            case "1":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getGarbage1().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "2":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getGarbage2().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "3":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週三不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getGarbage3().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "4":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getGarbage4().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "5":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getGarbage5().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "6":
                switch (this.getCity()) {
                    case "Taipei":
                        result = true;
                        break;
                    case "NewTaipei":
                        if (this.getGarbage6().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
            case "7":
                switch (this.getCity()) {
                    case "Taipei":
                        result = false; //台北市週日不收垃圾
                        break;
                    case "NewTaipei":
                        if (this.getGarbage7().equals("Y")) {
                            result = true;
                        }
                        break;
                    default:
                        result = false;
                }
                break;
        }
        return result;
    }

    public String getMemo() {
        Time t = new Time();
        String memo = "";

        if (this.getCity().equals("Taipei")) {
            switch (t.getDayOfWeekNumber()) {
                case "1":
                case "5":
                    memo = "今天資源回收可收：平面類：紙類 舊衣類 乾淨塑膠袋";
                case "2":
                case "4":
                case "6":
                    memo = "今天資源回收可收：乾淨保麗龍, 一般類（瓶罐、容器、小家電等)";
            }
        }

        if (this.getCity().equals("NewTaipei")) {
            memo =  getString("memo");
        }

        return memo;

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
