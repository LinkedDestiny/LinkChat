package com.link.platform.wifi;

import android.net.wifi.WifiConfiguration;

import com.link.platform.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class WiFiConfigurationFactory {

    public final static String DEVICE_DEFAULT = "Default";

    public static WiFiConfigurationFactory Instance = null;

    public static WiFiConfigurationFactory getInstance() {
        if( Instance == null ) {
            synchronized (WiFiConfigurationFactory.class ) {
                if( Instance == null ) {
                    Instance = new WiFiConfigurationFactory();
                }
            }
        }
        return Instance;
    }

    private WiFiConfigurationFactory() {

    }

    public WifiConfiguration getConfiguration( WifiConfiguration config , String name,  String password ) {
        config.SSID = Utils.WIFI_PREFIX + name;
        config.BSSID = Utils.WIFI_PREFIX + name;
        String device = android.os.Build.MODEL.substring(0, 3);
        try {
            try {
                String method_name = "get" + device + "Configuration";
                Method method = getClass().getMethod(method_name, WifiConfiguration.class, String.class);

                return (WifiConfiguration) method.invoke(this, config, password);
            } catch ( NoSuchMethodException err ) {
                String method_name = "get" + DEVICE_DEFAULT + "Configuration";
                Method method = getClass().getMethod(method_name, WifiConfiguration.class, String.class);
                return (WifiConfiguration) method.invoke(this, config, password);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    public WifiConfiguration getDefaultConfiguration( WifiConfiguration config , String password ) {
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        if( !password.equals("") ){
            config.preSharedKey = password;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedProtocols.set( WifiConfiguration.Protocol.WPA );
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        }
        return config;
    }

    public WifiConfiguration getHTCConfiguration( WifiConfiguration config , String password ) throws Exception {
        Field localField;
        localField = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
        localField.setAccessible(true);

        Object localObject = localField.get(config);
        localField.setAccessible(false);

        if( localObject != null ) {
            // set SSID
            setField( localObject , Utils.SSID , config.SSID );

            // set BSSID
            setField( localObject , Utils.BSSID , config.BSSID );

            //set DHCP state
            setField( localObject , Utils.DHCP , 1 );

            // set security mode
            if( password.equals("") ){
                Field OPEN = localObject.getClass().getDeclaredField( Utils.OPEN );

                setField( localObject , Utils.SECURE_TYPE , OPEN.get(OPEN) );
            } else {
                Field WPA2 = localObject.getClass().getDeclaredField( Utils.WPA2 );

                setField( localObject , Utils.SECURE_TYPE , WPA2.get(WPA2) );
                setField( localObject , Utils.KEY , password );
            }

        }
        return config;
    }

    private void setField( Object localObject , String name , Object value ) throws Exception {
        Field SSID = localObject.getClass().getDeclaredField(name);
        SSID.setAccessible(true);
        SSID.set(localObject, value);
        SSID.setAccessible(false);
    }

}
