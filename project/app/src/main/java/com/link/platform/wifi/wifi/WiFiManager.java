package com.link.platform.wifi.wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.link.platform.MainApplication;
import com.link.platform.util.UIHelper;
import com.link.platform.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class WiFiManager {

    public final static String TAG = "WiFiManager";

    public static WiFiManager Instance = null;

    public static WiFiManager getInstance() {
        if( Instance == null ) {
            synchronized ( WiFiManager.class ) {
                if( Instance == null ) {
                    Instance = new WiFiManager();
                }
            }
        }
        return Instance;
    }

    private WifiManager wifi;
    private WifiInfo info;
    private List<ScanResult> list = new ArrayList<ScanResult>();

    private WiFiManager() {
        wifi = (WifiManager) MainApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        info = wifi.getConnectionInfo();
    }

    public void openWiFi() {
        new WifiAsyncTask(MainApplication.getInstance().getApplicationContext() , true ).execute();
    }

    public void closeWiFi() {
        new WifiAsyncTask(MainApplication.getInstance().getApplicationContext() , false ).execute();
    }

    public boolean isOpen() {
        return wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public int getWiFiState() {
        return wifi.getWifiState();
    }

    public WifiInfo getWiFiInfo(){
        return info;
    }

    public void StartScan(){
        wifi.startScan();
    }

    public List<ScanResult> GetWifiList(){
        list = wifi.getScanResults();
        if( list == null ) {
            list = new ArrayList<ScanResult>();
        }

        Iterator<ScanResult> iter = list.iterator();
        while( iter.hasNext() ){
            ScanResult temp = iter.next();

            Log.d( TAG , temp.SSID );
            if( temp.SSID.length() < 6 ){
                iter.remove();
                continue;
            }
            String name = temp.SSID.substring(0, 6);
            Log.d(TAG , name );
            if( ! name.equals(Utils.WIFI_PREFIX ) ){
                iter.remove();
            }
        }
        return list;
    }

    private boolean setWiFiState(boolean enable) {

        wifi.setWifiEnabled( enable );
        int loopMax = 10;
        while( loopMax > 0 ){
            if( wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED ) {
                return true;
            }
            Log.d(TAG, "open wifi: waiting, pass: " + (10 - loopMax));
            try {
                Thread.sleep(500);
                loopMax--;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    private class WifiAsyncTask extends AsyncTask<Object,Object,Object> {
        ProgressDialog d;
        boolean mMode;

        public WifiAsyncTask(Context context, boolean mMode) {
            d = new ProgressDialog(context);
            this.mMode = mMode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d.setTitle("Turning WiFi " + (mMode?"on":"off") + "...");
            d.setMessage("...please wait a moment.");
            d.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            boolean result = Boolean.valueOf(o.toString());
            d.dismiss();

            UIHelper.makeToast("Turning WiFi " + (mMode?"on":"off") + ( result ? " successful." : "failed."));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return setWiFiState(mMode);
        }
    }



}
