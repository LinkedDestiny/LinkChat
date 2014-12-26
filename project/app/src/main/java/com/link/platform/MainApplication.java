package com.link.platform;

import android.app.Application;
import android.content.Context;

import com.link.platform.media.audio.AudioInstance;
import com.link.platform.util.Utils;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class MainApplication extends Application {

    public static MainApplication Instance = null;

    static {
        System.loadLibrary("NativeAudioCodec");
    }

    public void onCreate() {
        super.onCreate();
        Utils.init();
        Instance = this;

        AudioInstance.getInstance();
    }

    public static MainApplication getInstance(){
        return Instance;
    }

    public static Context getContext() {
        return Instance.getApplicationContext();
    }

}
