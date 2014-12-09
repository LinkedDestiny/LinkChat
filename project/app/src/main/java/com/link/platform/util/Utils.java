package com.link.platform.util;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class Utils {

    public final static String SSID = "SSID";
    public final static String BSSID = "BSSID";
    public final static String DHCP = "dncpEnable";
    public final static String WPA2 = "WPA2";
    public final static String OPEN = "OPEN";
    public final static String SECURE_TYPE = "secureType";
    public final static String KEY = "key";


    public final static String WIFI_PREFIX = "Linked";

    public static int WIFI_AP_STATE_UNKNOWN = -1;
    public static int WIFI_AP_STATE_DISABLING = 0;
    public static int WIFI_AP_STATE_DISABLED = 1;
    public static int WIFI_AP_STATE_ENABLING = 2;
    public static int WIFI_AP_STATE_ENABLED = 3;
    public static int WIFI_AP_STATE_FAILED = 4;

    public final static int BUFFER_SIZE = 8192;
    public final static long SELECT_TIMEOUT = 60 * 1000;
}