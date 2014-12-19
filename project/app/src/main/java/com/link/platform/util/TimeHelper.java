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
        str +=c.get(Calendar.MONTH);
        str +=c.get(Calendar.DAY_OF_MONTH);
        str +=c.get(Calendar.HOUR);
        str +=c.get(Calendar.MINUTE);
        str +=c.get(Calendar.SECOND);
        return str;
    }
}
