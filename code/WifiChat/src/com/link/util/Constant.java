package com.link.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import android.os.Environment;
import android.util.Log;

public class Constant {
	
	//HTC device type
	public static String HTC = "HTC";
	
	public static String VOICE_PATH = GET_PATH();
	
	public static String GET_PATH(){
		String path = Environment.getExternalStorageDirectory().getPath() + "/WifiHot/voicecache/";
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			Log.e("Constant" , "No SD CARD ");
		}
		File file = new File( path );
		if( !file.exists() ){
			file.mkdirs();
		}
		return path;
	}
	
	//Wifi AP state
	public static int WIFI_AP_STATE_UNKNOWN = -1;
	public static int WIFI_AP_STATE_DISABLING = 0;
	public static int WIFI_AP_STATE_DISABLED = 1;
	public static int WIFI_AP_STATE_ENABLING = 2;
	public static int WIFI_AP_STATE_ENABLED = 3;
	public static int WIFI_AP_STATE_FAILED = 4;
	
	public static String[] WIFI_AP_STATE_TEXT = new String[]{
		"Disabling" , "Disabled" , "Enabling" , "Enabled" , "Failed"
	};
	
	// Wifi AP TAG
	public static String TAG = "Linked";
	
	
	// char decoder
	public static Charset charset = Charset.forName("UTF-8");
	public static CharsetDecoder decoder = charset.newDecoder();
	
	// socket
	public static int BUFFER_SIZE = 1024;
	public static int SERVER_PORT = 6666;
	public static int CLIENT_PORT = 6666;
	
	// read and write state
	public static int IO_NO_CHANNEL = -1;
	public static int IO_FAILURE = 0;
	public static int IO_SUCCESS = 1;

	
	// DefaultByteBuffer
	public static int BUFF_LENGTH = 3;
	public static int BUFF_IP = 4;
	public static int BUFF_TYPE = 2;
	public static int BUFF_MESSAGE = 8192;
	
	// Message Type
	public static String MESSAGE = "1";
	
	public static String MESSAGE_CLIENT_MESSAGE = "01";
	public static String MESSAGE_CLIENT_USERINFO = "02";
	public static String MESSAGE_CLIENT_VOICE = "03";
	
	public static String MESSAGE_SERVER_MESSAGE = "11";
	public static String MESSAGE_SERVER_UPDATE_USER_LIST = "12";
	public static String MESSAGE_SERVER_USER_MESSAGE = "13";
	public static String MESSAGE_SERVER_USER_VOICE = "14";
	
	// scale
	public static float SCALE;
	
	public static String VOICE_BEGIN = "$VOICE_BETIN$";
	public static String VOICE_END = "$VOICE_END$";
	
	
}
