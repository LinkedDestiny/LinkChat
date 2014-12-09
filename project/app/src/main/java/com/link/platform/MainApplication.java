package com.link.platform;

import android.app.Application;
import android.content.Context;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class MainApplication extends Application {

    public static MainApplication Instance = null;

    public void onCreate() {
        super.onCreate();

        Instance = this;
    }

    public static MainApplication getInstance(){
        return Instance;
    }

    public static Context getContext() {
        return Instance.getApplicationContext();
    }

}
