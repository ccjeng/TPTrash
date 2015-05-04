package com.oddsoft.tpetrash2.utils;

import java.util.Calendar;

/**
 * Created by andycheng on 2015/5/4.
 */
public class Time {

    private Calendar calendar;
    private int dayOfWeek;

    public Time() {
        Calendar calendar = Calendar.getInstance();
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public String getDayOfWeekNumber() {

        return String.valueOf(dayOfWeek);
    }


    public String getDayOfWeekName() {

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
}
