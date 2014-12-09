package com.link.wifi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.link.listener.AsyncTaskListener;
import com.link.util.Constant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

public class WifiAP {
	
	private Activity activity;
	private int constant = 0;
	
	private WifiManager wifi;
	private AsyncTaskListener listener;
	
	private String ap_name;
	private String ap_password;
	private boolean ap_type;	// false means no password , true means need password
	
	private String TAG = "WifiAP";
	private int stateWifiWasIn = -1;
	
	private boolean alwaysEnableWifi = true;
	
	public WifiAP( Context context , AsyncTaskListener listener ){
		this.activity = ( Activity ) context;
		this.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		this.listener = listener;
	}
	
	 /**
     * Toggle the WiFi AP state
     * @param wifihandler
     * @author http://stackoverflow.com/a/7049074/1233435
     */
    public void toggleWiFiAP() {
    	
        boolean wifiApIsOn = getWifiAPState() == Constant.WIFI_AP_STATE_ENABLED 
        		|| getWifiAPState() == Constant.WIFI_AP_STATE_ENABLING;
        
        Log.d( TAG , "WifiAPState : " + wifiApIsOn );
        
        new SetWifiAPTask( !wifiApIsOn , false , activity ).execute();
    }
    
    /**
     * set the AP info
     * @param name SSID of AP
     * @param password 
     */
	public void setWifiAPInfo( String name , String password ){
    	this.ap_name = name;
    	this.ap_password = password;
    	if( password.equals( "" ) ){
    		ap_type = false;
    	}else{
    		ap_type = true;
    	}
    	
    	Log.v(TAG , ap_type + " " );
    }

	/**
	 * set the WifiConfiguration of the AP
	 * @param config
	 */
    private void setWifiConfiguration( WifiConfiguration config ){
    	Log.i( TAG , "****  setWifiConfiguration   ****" );
    	config.SSID = Constant.TAG + this.ap_name;
    	
    	String device = android.os.Build.MODEL.substring(0, 3);
    	Log.i( TAG , "Device type : " + device.equals(Constant.HTC) );
    	if ( device.equals( Constant.HTC ) ) {	// AP setting for HTC device
    		
    		Log.i( TAG , "This is HTC device" );
    		
    		Field localField1;  
    		try {  

                localField1 = WifiConfiguration.class.getDeclaredField("mWifiApProfile");  

                localField1.setAccessible(true);  

                Object localObject2 = localField1.get(config);  

                localField1.setAccessible(false);  
                if(localObject2!=null){  

                	// set SSID
                    Field SSID = localObject2.getClass().getDeclaredField("SSID");  
                    SSID.setAccessible(true);  
                    SSID.set(localObject2, config.SSID); 
                    SSID.setAccessible(false);  
                    
                    // set BSSID
                    Field BSSID = localObject2.getClass().getDeclaredField("BSSID");
                    BSSID.setAccessible(true);
                    BSSID.set(localObject2, config.BSSID);
                    BSSID.setAccessible(false);
                    
                    if( this.ap_type ){
                    	Field WPA2 = localObject2.getClass().getDeclaredField("WPA2");
                    	
	                    //set secureType
	                    Field secureType = localObject2.getClass().getDeclaredField("secureType");
	                    secureType.setAccessible(true);
	                    secureType.set( localObject2, WPA2.get(WPA2) );
	                    secureType.setAccessible(false);
	                    
	                    //set key
	                    Field key = localObject2.getClass().getDeclaredField("key");
	                    key.setAccessible(true);
	                    key.set(localObject2, this.ap_password );
	                    key.setAccessible(false);
	                    
                    }else{
                    	
                    	Field OPEN = localObject2.getClass().getDeclaredField("OPEN");
                    	
	                    //set secureType
	                    Field secureType = localObject2.getClass().getDeclaredField("secureType");
	                    secureType.setAccessible(true);
	                    secureType.set( localObject2, OPEN.get(OPEN) );
	                    secureType.setAccessible(false);
                    }
                      
                    //set DHCP state
                    Field DHCP = localObject2.getClass().getDeclaredField("dhcpEnable");  
                    DHCP.setAccessible(true);    
                    DHCP.setInt(localObject2, 1);  
                    DHCP.setAccessible(false);  

                }  

            } catch(Exception e) {  
            	Log.e( TAG, "HTC device setting error" );
                e.printStackTrace();  

            }  
    		
    	} else {	// AP setting for other devices
    		Log.i( TAG , "This is normal device" );
	        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	        if( ap_type ){
	        	config.preSharedKey = this.ap_password;
		        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                         
		        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		        config.allowedProtocols.set( WifiConfiguration.Protocol.RSN );
		        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
		        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        }
    	}
    }
   
	
    /**
     * Enable/disable wifi
     * @param true or false
     * @return WifiAP state
     * @throws NoSuchMethodException 
     */
    private int setWifiApEnabled(boolean enabled) {
        Log.d(TAG, "*** setWifiApEnabled CALLED **** " + enabled);

        WifiConfiguration config = new WifiConfiguration();
        
        this.setWifiConfiguration(config);
        //remember wirelesses current state
        if ( enabled && stateWifiWasIn == -1 ){
            stateWifiWasIn = wifi.getWifiState();
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
        int state = Constant.WIFI_AP_STATE_UNKNOWN;
        
        try {
            Log.d(TAG, (enabled?"enabling":"disabling") +" wifi ap: calling");
            wifi.setWifiEnabled(false);
            
            Method method1 = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
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
            		( getWifiAPState() == Constant.WIFI_AP_STATE_DISABLING 
            			|| getWifiAPState() == Constant.WIFI_AP_STATE_ENABLED 
            			|| getWifiAPState() == Constant.WIFI_AP_STATE_FAILED ) ) {
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
            if( stateWifiWasIn == WifiManager.WIFI_STATE_ENABLED 
            		|| stateWifiWasIn == WifiManager.WIFI_STATE_ENABLING 
            		|| stateWifiWasIn == WifiManager.WIFI_STATE_UNKNOWN 
            		|| alwaysEnableWifi ) {
            	
                Log.d(TAG, "enable wifi: calling");
                wifi.setWifiEnabled(true);
                //don't hold things up and wait for it to get enabled
            }

            stateWifiWasIn = -1;
            
        } else if (enabled) {
        	
            int loopMax = 10;
            while ( loopMax > 0 && 
            		( getWifiAPState() == Constant.WIFI_AP_STATE_ENABLING 
            		|| getWifiAPState() == Constant.WIFI_AP_STATE_DISABLED 
            		|| getWifiAPState() == Constant.WIFI_AP_STATE_FAILED ) ) {
            	
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
    
	 /**
     * Get the wifi AP state
     * @return WifiAP state
     */
    public int getWifiAPState() {
        int state = Constant.WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {

        }

        if(state>=10){
            //using Android 4.0+ (or maybe 3+, haven't had a 3 device to test it on) so use states that are +10
            constant=10;
        }

        //reset these in case was newer device
        Constant.WIFI_AP_STATE_DISABLING = 0+constant;
        Constant.WIFI_AP_STATE_DISABLED = 1+constant;
        Constant.WIFI_AP_STATE_ENABLING = 2+constant;
        Constant.WIFI_AP_STATE_ENABLED = 3+constant;
        Constant.WIFI_AP_STATE_FAILED = 4+constant;

        return state;
    }
	
	/**
     * the AsyncTask to enable/disable the wifi ap
     */
    class SetWifiAPTask extends AsyncTask<Void, Void, Void> {
        boolean mMode; 				//enable or disable wifi AP
        boolean mFinish;			//finalize or not (e.g. on exit)
        ProgressDialog d;

        /**
         * enable/disable the wifi ap
         * @param mode enable or disable wifi AP
         * @param finish finalize or not (e.g. on exit)
         * @param context the context of the calling activity
         */
        public SetWifiAPTask(boolean mode, boolean finish, Context context) {
            mMode = mode;
            mFinish = finish;
            d = new ProgressDialog(context);
        }

        /**
         * do before background task runs
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d.setTitle("Turning WiFi AP " + (mMode?"on":"off") + "...");
            d.setMessage("...please wait a moment.");
            d.show();
        }

        /**
         * do after background task runs
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                d.dismiss();
                
                //tell the activity that the task is finished
                listener.onPostExecute( true );
                
            } catch (IllegalArgumentException e) {

            }
            
            if (mFinish){
            	//activity.finish();
            }
        }

        /**
         * the background task to run
         * @param params
         */
        @Override
        protected Void doInBackground(Void... params) {
        	setWifiApEnabled(mMode);
            return null;
        }
    }
    
}
