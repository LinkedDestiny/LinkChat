package com.link.platform.util;

import android.widget.Toast;

import com.link.platform.MainApplication;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class UIHelper {

    public static void makeToast(String message) {
        Toast.makeText(MainApplication.getInstance().getApplicationContext() , message , Toast.LENGTH_SHORT ).show();
    }
}
