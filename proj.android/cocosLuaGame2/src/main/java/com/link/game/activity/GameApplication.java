package com.link.game.activity;

import android.app.Application;

/**
 * Created by danyang.ldy on 2015/1/21.
 */
public class GameApplication extends Application {

    public static GameApplication Instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Instance = this;
    }
}
