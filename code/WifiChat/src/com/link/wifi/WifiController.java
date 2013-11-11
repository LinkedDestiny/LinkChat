package com.link.wifi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.link.util.Constant;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiController {
	
	private WifiManager wifi;
	private WifiInfo info;
	private List<ScanResult> list = new ArrayList<ScanResult>();
	private WifiLock lock;
	
	public  WifiController(Context context){
		
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	
		info = wifi.getConnectionInfo();
	}
	
	public void openWifi(){
		wifi.setWifiEnabled( true );
		
		int loopMax = 10;
        while( loopMax>0 && 
        		wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED ){
        	
            Log.d("WifiController", "open wifi: waiting, pass: " + (10-loopMax));
            try {
                Thread.sleep(500);
                loopMax--;
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
	
	public void closeWifi(){
		wifi.setWifiEnabled( false );
	}
	
	public int getWifiState(){
		return wifi.getWifiState();
	}
	public void AcquireWifiLock(){
		lock.acquire();
	}
	
	public void ReleaseWifiLock(){
	 
	  if (lock.isHeld()){
		  lock.acquire();
	  }
	}

	public void CreatWifiLock(){
		lock = wifi.createWifiLock("Test");
	}
	
	public WifiInfo getWifiInfo(){
		return info;
	}
	
	/**
	 * start to scan the available wifi
	 */
	public void StartScan(){
		wifi.startScan();
		list = wifi.getScanResults();
	}
	
	/**
	 * get the available wifi list
	 * @return
	 */
	public List<ScanResult> GetWifiList(){
		
		if( list == null )
			return null;
		Iterator<ScanResult> iter = list.iterator();
		
		while( iter.hasNext() ){
			ScanResult temp = ( ScanResult ) iter.next();
			
			Log.v("Wifi" , temp.SSID );
			if( temp.SSID.length() < 6 ){
				iter.remove();
				continue;
			}
			String TAG = temp.SSID.substring(0, 6);
			Log.v("Wifi" , TAG );
			if( ! TAG.equals( Constant.TAG ) ){
				iter.remove();
			}
		}
		return list;
	}
	
	/**
	 * 	connect to the wifi defined by the wcg
	 * @param wcg
	 */
	public void addNetWork( WifiConfiguration wcg ){
		int wcgID = wifi.addNetwork(wcg);
		System.out.println( "set Wifi connect " +  wifi.enableNetwork(wcgID, true) );
	}
	
	/**
	 *  create a WifiConfiguration+
	 * @param SSID		name of wifi
	 * @param Password	
	 * @param BBSID		
	 * @param type		WEP , WAP2 or open
	 * @return
	 */
	public WifiConfiguration CreateWifiConfiguration(String SSID, String Password, String BBSID , int type)  
    {  
		wifi.disconnect();
		
		WifiConfiguration temp = IsExsits( SSID );
		if( temp != null ) {
			wifi.removeNetwork( temp.networkId );
		}
		
		WifiConfiguration config = new WifiConfiguration();    
		config.SSID = "\"" + SSID + "\"";
		config.BSSID = BBSID;
		config.hiddenSSID = true;
		config.status = WifiConfiguration.Status.ENABLED;
	        
		if( type == 1 ){
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			
		}else if( type == 3 ){
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
	
	/**
	 * get the IP list connected to the host
	 * @return
	 *  ArrayList<String> ip list
	 */
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
