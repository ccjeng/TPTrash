package com.oddsoft.tpetrash2.adapter;

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

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVObject;
import com.oddsoft.tpetrash2.utils.Constant;

import java.text.DecimalFormat;

@AVClassName(Constant.LEANCLOUD_OBJECT_NAME)
public class ArrayItem extends AVObject {

    public String getAddress() {

        String address = getString("address");

        if (this.getCity().equals("Taipei")) {
            if (address.length()>6) {
                return "[" + getRegion() + "] " + address.substring(6, address.length());
            } else {
                return "[" + getRegion() + "] " + address;
            }
        } else {
            return "[" + getRegion() + "] " + address;
        }

    }

    public String getFullAddress() {
        return  getString("address");
    }

    public String getCarNumber() {
        return " [" +getLine() + " #" + getCarNo() + "]";
    }

    public String getCarTime() {
        return getString("time") + " " + getCarNumber();
    }

    public Integer getCarStartTime() {
        String startTime = getString("time");

        try {
            if (startTime.contains("-")) {
                startTime = startTime.split("-")[0];
            } else if(startTime.contains("~")) {
                startTime = startTime.split("~")[0];
            }
        } catch (Exception e) {
            startTime="00";
            e.printStackTrace();
        }

        return Integer.valueOf(startTime.replace("01:","25:").replace("：", "").replace(":","").trim());
    }

    public Integer getCarEndTime() {
        String endTime = getString("time");

        try {
            if (endTime.contains("-")) {
                endTime = endTime.split("-")[1];
            } else if(endTime.contains("~")) {
                endTime = endTime.split("~")[1];
            } else {
                endTime = String.valueOf(getCarStartTime()+3);
            }
        } catch (Exception e) {
            endTime = "00";
            e.printStackTrace();
        }

        return Integer.valueOf(endTime.replace("01:", "25:").replace("：", "").replace(":", "").replace(" ","").trim());
    }

    public String getCarHour() {
        return getString("hour");
    }

    public String getLineID() {
        return getString("lineid");
    }

    public String getLine() {
        return getString("line");
    }

    public String getCarNo() {
        return getString("carno");
    }

    //里
    public String getLi() {
        return getString("li");
    }

    //區
    public String getRegion() {
        return getString("region");
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

    //判斷今天要不要收廚餘
    public Boolean checkTodayAvailableFood(String day) {
        Boolean result = false;
        switch (day) {
            case "1":
                result = this.getFood1().equals("Y") ? true : false;
                break;
            case "2":
                result = this.getFood2().equals("Y") ? true : false;
                break;
            case "3":
                result = this.getFood3().equals("Y") ? true : false;
                break;
            case "4":
                result = this.getFood4().equals("Y") ? true : false;
                break;
            case "5":
                result = this.getFood5().equals("Y") ? true : false;
                break;
            case "6":
                result = this.getFood6().equals("Y") ? true : false;
                break;
            case "0":
                result = this.getFood7().equals("Y") ? true : false;
                break;
        }

        return result;
    }

    //判斷今天要不要收資源回收
    public Boolean checkTodayAvailableRecycling(String day) {
        Boolean result = false;
        switch (day) {
            case "1":
                result = this.getRecycling1().equals("Y") ? true : false;
                break;
            case "2":
                result = this.getRecycling2().equals("Y") ? true : false;
                break;
            case "3":
                result = this.getRecycling3().equals("Y") ? true : false;
                break;
            case "4":
                result = this.getRecycling4().equals("Y") ? true : false;
                break;
            case "5":
                result = this.getRecycling5().equals("Y") ? true : false;
                break;
            case "6":
                result = this.getRecycling6().equals("Y") ? true : false;
                break;
            case "0":
                result = this.getRecycling7().equals("Y") ? true : false;
                break;
        }
        return result;
    }

    //判斷今天要不要收一般垃圾
    public Boolean checkTodayAvailableGarbage(String day) {
        Boolean result = false;
        switch (day) {
            case "1":
                result = this.getGarbage1().equals("Y") ? true : false;
                break;
            case "2":
                result = this.getGarbage2().equals("Y") ? true : false;
                break;
            case "3":
                result = this.getGarbage3().equals("Y") ? true : false;
                break;
            case "4":
                result = this.getGarbage4().equals("Y") ? true : false;
                break;
            case "5":
                result = this.getGarbage5().equals("Y") ? true : false;
                break;
            case "6":
                result = this.getGarbage6().equals("Y") ? true : false;
                break;
            case "0":
                result = this.getGarbage7().equals("Y") ? true : false;
                break;
        }
        return result;
    }

    public String getMemo(String day) {
        String memo = getString("memo");
        if (this.getCity().equals("Taipei")) {

            switch (day) {
                case "1":
                case "5":
                    if (memo.equals("")) {
                        memo = "資源回收：平面類：紙類 舊衣類 乾淨塑膠袋";
                    } else {
                        memo = memo + ", 資源回收：平面類：紙類 舊衣類 乾淨塑膠袋";
                    }
                    break;
                case "2":
                case "4":
                case "6":
                    if (memo.equals("")) {
                        memo = "資源回收：乾淨保麗龍, 一般類（瓶罐、容器、小家電等)";
                    } else {
                        memo = memo + ", 資源回收：乾淨保麗龍, 一般類（瓶罐、容器、小家電等)";
                    }
                    break;
            }
        }

        if (this.getCity().equals("NewTaipei")) {

            memo =  getString("memo");
            if (memo == null)
                memo = "";
        }

        //memo = this.getFood7() + this.getRecycling7() + this.getGarbage7();

        //memo = this.checkTodayAvailableFood() +"-"+ this.checkTodayAvailableRecycling() +"-"+ this.checkTodayAvailableGarbage();

        return memo;

    }


    public AVGeoPoint getLocation() {
        return getAVGeoPoint("location");
    }


    public String getDistance(AVGeoPoint current) {

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

    public static final Creator CREATOR = AVObjectCreator.instance;

    /*
    public static ParseQuery<ArrayItem> getQuery() {
        return ParseQuery.getQuery(ArrayItem.class);
    }
*/

}
