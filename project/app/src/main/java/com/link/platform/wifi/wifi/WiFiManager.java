package com.link.platform.wifi.wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.link.platform.MainApplication;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.util.StringUtil;
import com.link.platform.util.UIHelper;
import com.link.platform.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private Context context;

    private WiFiManager() {
        wifi = (WifiManager) MainApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        info = wifi.getConnectionInfo();
    }

    public void setContext( Context context ) {
        this.context = context;
    }

    public void clear() {
        this.context = null;
    }

    public void openWiFi() {
        new WifiAsyncTask(context , true ).execute();
    }

    public void closeWiFi() {
        new WifiAsyncTask(context , false ).execute();
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
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object element = it.next();
            if (set.add(element))
                newList.add(element);
        }
        list.clear();
        list.addAll(newList);
        return list;
    }

    public void addNetWork( WifiConfiguration wcg ){
        int wcgID = wifi.addNetwork(wcg);
        Log.d(TAG,  "set Wifi connect " + wcgID + ": " +  wifi.enableNetwork(wcgID, true) );
        new AddAsyncTask(context, wcg.SSID).execute();
    }

    public WifiConfiguration CreateWifiConfiguration(String SSID, String Password) {
        wifi.disconnect();

        WifiConfiguration temp = IsExsits( SSID );
        if( temp != null ) {
            wifi.removeNetwork( temp.networkId );
        }

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + SSID + "\"";
        //config.status = WifiConfiguration.Status.ENABLED;

        if(StringUtil.isBlank(Password)){
            //config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = "\"\"";
            Log.d(TAG, "no password");
        } else {
            config.hiddenSSID = true;
            config.preSharedKey = "\""+Password+"\"";
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }

        return config;
    }

    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifi.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\""+SSID+"\"")) {
                return existingConfig;
            }
        }
        return null;
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

    private boolean connectWiFi() {
        ConnectivityManager conMan = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        int loop = 60;
        while( loop > 0 && conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
                getDetailedState() != NetworkInfo.DetailedState.CONNECTED ) {
            loop--;
            Log.d(TAG, conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
                    getDetailedState().toString());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ( conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
                getDetailedState() == NetworkInfo.DetailedState.CONNECTED );
    }

    private class AddAsyncTask extends AsyncTask<Object,Object,Object> {
        ProgressDialog d;
        String room;

        public AddAsyncTask(Context context, String room) {
            d = new ProgressDialog(context);
            this.room = room;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d.setTitle("Connect to " + room + "...");
            d.setMessage("...please wait a moment.");
            d.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            boolean result = Boolean.valueOf(o.toString());
            d.dismiss();

            UIHelper.makeToast("Connect to " + room + ( result ? " successful." : "failed."));

            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId( MessageTable.MSG_CONNECT_WIFI_FINISH );
            msg.setObject( result );
            MessageCenter.getInstance().sendMessage(msg);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return connectWiFi();
        }
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

            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId( mMode ? MessageTable.MSG_OPEN_WIFI_FINISH : MessageTable.MSG_CLOSE_WIFI_FINISH );
            msg.setObject( result );
            MessageCenter.getInstance().sendMessage(msg);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return setWiFiState(mMode);
        }
    }

    public ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedIP;
    }

    public int getHostIP( ){
        DhcpInfo dhcpinfo = wifi.getDhcpInfo();
        return dhcpinfo.serverAddress;
    }

    public int getIP(){
        DhcpInfo dhcpinfo = wifi.getDhcpInfo();
        return dhcpinfo.ipAddress;
    }

}
