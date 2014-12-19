package com.link.platform.wifi.ap;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.link.platform.MainApplication;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.util.UIHelper;
import com.link.platform.util.Utils;
import com.link.platform.wifi.WiFiConfigurationFactory;
import com.link.platform.wifi.wifi.WiFiManager;

import java.lang.reflect.Method;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class APManager {

    public final static String TAG = "APManager";

    public static APManager Instance = null;

    public static APManager getInstance() {
        if( Instance == null ) {
            synchronized (APManager.class) {
                if( Instance == null ) {
                    Instance = new APManager();
                }
            }
        }
        return Instance;
    }

    private WifiManager wifi;
    private String ap_name;
    private String ap_password;

    private int constant = 0;
    private WifiInfo last_wifi;
    private int last_state;

    private APManager() {
        wifi = (WifiManager) MainApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void setWiFiAPInfo( String name , String password ){
        this.ap_name = name;
        this.ap_password = password;
    }

    public void toggleWiFiAP( Context context , boolean enable) {
        Log.d(TAG, "WifiAPState : " + enable);
        new APAsyncTask( context , enable ).execute();
    }

    public int getWiFiAPState() {
        int state = Utils.WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {

        }

        if(state >= 10){
            //using Android 4.0+ (or maybe 3+, haven't had a 3 device to test it on) so use states that are +10
            constant = 10;
        }

        //reset these in case was newer device
        Utils.WIFI_AP_STATE_DISABLING = 0 + constant;
        Utils.WIFI_AP_STATE_DISABLED = 1 + constant;
        Utils.WIFI_AP_STATE_ENABLING = 2 + constant;
        Utils.WIFI_AP_STATE_ENABLED = 3 + constant;
        Utils.WIFI_AP_STATE_FAILED = 4 + constant;

        return state;
    }

    private int setWifiApEnabled(boolean enabled) {
        Log.d(TAG, "*** setWifiApEnabled CALLED **** " + enabled);

        WifiConfiguration config = new WifiConfiguration();

        config = WiFiConfigurationFactory.getInstance().getConfiguration( config, this.ap_name, this.ap_password);
        //remember wirelesses current state
        if ( enabled && WiFiManager.getInstance().isOpen() ){
            last_wifi = WiFiManager.getInstance().getWiFiInfo();
            last_state = WiFiManager.getInstance().getWiFiState();
        }

        //disable wireless
        if ( enabled && wifi.getConnectionInfo() !=null) {
            Log.d(TAG, "disable wifi: calling");
            wifi.setWifiEnabled(false);

            int loopMax = 10;
            while( loopMax>0 &&
                    wifi.getWifiState() != WifiManager.WIFI_STATE_DISABLED ){

                Log.d(TAG, "disable wifi: waiting, pass: " + (10-loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "disable wifi: done, pass: " + (10-loopMax));
        }

        //enable/disable wifi ap
        int state = Utils.WIFI_AP_STATE_UNKNOWN;

        try {
            Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: calling");
            wifi.setWifiEnabled(false);

            Method method1 = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            Log.d(TAG , config.SSID);
            method1.invoke(wifi, config, enabled); // true

            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //hold thread up while processing occurs
        if (!enabled) {
            int loopMax = 10;
            while ( loopMax > 0 &&
                    ( getWiFiAPState() == Utils.WIFI_AP_STATE_DISABLING
                            || getWiFiAPState() == Utils.WIFI_AP_STATE_ENABLED
                            || getWiFiAPState() == Utils.WIFI_AP_STATE_FAILED ) ) {
                Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: waiting, pass: " + (10-loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: done, pass: " + (10-loopMax));

            //enable wifi if it was enabled beforehand
            //this is somewhat unreliable and app gets confused
            //and doesn't turn it back on sometimes so added toggle to always enable if you desire
            if( last_state == WifiManager.WIFI_STATE_ENABLED
                    || last_state == WifiManager.WIFI_STATE_ENABLING
                    || last_state == WifiManager.WIFI_STATE_UNKNOWN ) {

                Log.d(TAG, "enable wifi: calling");
                wifi.setWifiEnabled(true);
                //don't hold things up and wait for it to get enabled
            }

            last_state = -1;

        } else if (enabled) {

            int loopMax = 10;
            while ( loopMax > 0 &&
                    ( getWiFiAPState() == Utils.WIFI_AP_STATE_ENABLING
                            || getWiFiAPState() == Utils.WIFI_AP_STATE_DISABLED
                            || getWiFiAPState() == Utils.WIFI_AP_STATE_FAILED ) ) {

                Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: waiting, pass: " + (10-loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: done, pass: " + (10-loopMax));
        }
        return state;
    }

    private class APAsyncTask extends AsyncTask<Object,Object,Object> {
        ProgressDialog d;
        boolean mMode;

        public APAsyncTask(Context context, boolean mMode) {
            d = new ProgressDialog(context);
            this.mMode = mMode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d.setTitle("正在 " + (mMode?"创建":"关闭") + "房间...");
            d.setMessage("...please wait a moment.");
            d.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            boolean result = true;
            if( getWiFiAPState() == Utils.WIFI_AP_STATE_FAILED ) {
                result = false;
            }

            d.dismiss();

            UIHelper.makeToast((mMode ?"创建":"关闭") + "房间" + (result ? " 成功." : "失败."));
            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId( mMode ? MessageTable.MSG_OPEN_AP_FINISH : MessageTable.MSG_CLOSE_AP_FINISH );
            msg.setObject( mMode ? result : true);
            MessageCenter.getInstance().sendMessage(msg);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return setWifiApEnabled(mMode);
        }
    }
}
