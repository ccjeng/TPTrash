package com.oddsoft.tpetrash2.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andycheng on 2015/5/4.
 */
public class Time {
    private static final String TAG = "Time";
    private Calendar calendar;
    private static int dayOfWeek;

    public Time() {

    }

    public static String getDayOfWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        //dayOfWeek=7;
        return String.valueOf(dayOfWeek -1);
    }


    public static String getDayOfWeekName() {
        Calendar calendar = Calendar.getInstance();
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        String dayOfWeekName = "";
        switch(dayOfWeek){
            case Calendar.SUNDAY:
                dayOfWeekName = "星期日";
                break;
            case Calendar.MONDAY:
                dayOfWeekName = "星期一";
                break;
            case Calendar.TUESDAY:
                dayOfWeekName = "星期二";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeekName = "星期三";
                break;
            case Calendar.THURSDAY:
                dayOfWeekName = "星期四";
                break;
            case Calendar.FRIDAY:
                dayOfWeekName = "星期五";
                break;
            case Calendar.SATURDAY:
                dayOfWeekName = "星期六";
        }
        return dayOfWeekName;
    }

    //check if today is Chinese New Year
    public static Boolean isCNY() {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);

        try {
            Date todayDate = dateFormatter.parse(dateFormatter.format(new Date())); //Today
            Date fromDate = dateFormatter.parse("2016-02-08"); //2016-02-08
            Date toDate = dateFormatter.parse("2016-02-11"); //2016-02-11

            //Log.d(TAG, "Today = " + todayDate);
            //Log.d(TAG, "fromDate = " + fromDate);
            //Log.d(TAG, "toDate = " + toDate);

            if(todayDate.after(fromDate) && todayDate.before(toDate)) {
                // In between
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer getCurrentHHMM() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        String strHour = String.valueOf(hours);
        String strMin = String.valueOf(minutes);

        if (strHour.equals("0")) {
            strHour = "24";
        }
        if (strMin.length() == 1) {
            strMin = "0" + strMin;
        }

        return Integer.valueOf(strHour + strMin);

    }
}
