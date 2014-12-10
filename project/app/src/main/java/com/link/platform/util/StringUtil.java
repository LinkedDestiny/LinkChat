package com.link.platform.util;

/**
 * Created by danyang.ldy on 2014/12/10.
 */
public class StringUtil {

    public static boolean isBlank(String str) {
        if( str == null || str.equals("") ) {
            return true;
        }
        return false;
    }
}
