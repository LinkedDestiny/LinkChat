package com.link.platform.util;

import android.os.Environment;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class ImageLoader {

    private static ImageLoader Instance = null;

    public static ImageLoader getInstance() {
        if( Instance == null ) {
            synchronized (ImageLoader.class) {
                if( Instance == null ) {
                    Instance = new ImageLoader();
                }
            }
        }
        return Instance;
    }

    private String path;

    private ImageLoader() {
        path = Environment.getExternalStorageDirectory().getPath() + Utils.STORAGE_PATH + Utils.IMG_CACHE;
    }

}
