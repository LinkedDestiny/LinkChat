package com.link.platform.util;

import android.os.Environment;

import java.io.File;

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
    public final static String DEFAULT_REMARK = "天一宝宝";

    public static int WIFI_AP_STATE_UNKNOWN = -1;
    public static int WIFI_AP_STATE_DISABLING = 0;
    public static int WIFI_AP_STATE_DISABLED = 1;
    public static int WIFI_AP_STATE_ENABLING = 2;
    public static int WIFI_AP_STATE_ENABLED = 3;
    public static int WIFI_AP_STATE_FAILED = 4;

    public final static int BUFFER_SIZE = 8192 * 2;
    public final static int FILE_BUFFER_SIZE = BUFFER_SIZE - 12;
    public final static long SELECT_TIMEOUT = 30 * 1000;

    public static final int FILE_SELECT_CODE = 0;
    public static final int OPEN_CAMERA_CODE = 10;
    public static final int OPEN_GALLERY_CODE = 11;
    public static final int CROP_PHOTO_CODE = 12;

    public static String PATH;
    public static String SD_PATH;

    public final static String STORAGE_PATH = "/storage/";
    public final static String IMG_CACHE = "/IMG/";
    public final static String VOICE_CACHE = "/VOICE/";
    public final static String FILE_CACHE = "/FILE/";

    public final static String LOCAL_SETTING = "local_settings";

    public final static int CHAT_PORT = 9501;

    public static void init() {
        PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/com.link.platform";
        SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LinkChat";

        File dir = new File( SD_PATH + IMG_CACHE );
        if( !dir.exists() ) {
            dir.mkdirs();
        }
        dir = new File( SD_PATH + VOICE_CACHE );
        if( !dir.exists() ) {
            dir.mkdirs();
        }
        dir = new File( SD_PATH + FILE_CACHE );
        if( !dir.exists() ) {
            dir.mkdirs();
        }
    }
}
