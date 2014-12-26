package com.link.platform.util;

import java.util.Calendar;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class TimeHelper {

    public static int getIdByTime() {
        return currentTime().hashCode();
    }

    public static String currentTime() {
        Calendar c = Calendar.getInstance();
        String str = "";
        str += c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        if( month < 10 ) {
            str +="0";
        }
        str += month;
        int day = c.get(Calendar.DAY_OF_MONTH);
        if( day < 10 ) {
            str +="0";
        }
        str += day;
        int hour = c.get(Calendar.HOUR);
        if( hour < 10 ) {
            str +="0";
        }
        str += hour;
        int minute = c.get(Calendar.MINUTE);
        if( minute < 10 ) {
            str +="0";
        }
        str += minute;
        int second = c.get(Calendar.SECOND);
        if( second < 10 ) {
            str +="0";
        }
        str += second;
        return str;
    }
}
