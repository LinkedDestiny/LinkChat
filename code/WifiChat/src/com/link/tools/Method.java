package com.link.tools;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.link.info.DefaultCharBuffer;
import com.link.info.User;
import com.link.util.Constant;

public class Method {
	
	// change ip from int to String
	public static  String ipIntToString(int ip) { 
		try { 
			byte[] bytes = new byte[4]; 
			bytes[0] = (byte) (0xff & ip); 
			bytes[1] = (byte) ((0xff00 & ip) >> 8); 
			bytes[2] = (byte) ((0xff0000 & ip) >> 16); 
			bytes[3] = (byte) ((0xff000000 & ip) >> 24); 
			return Inet4Address.getByAddress(bytes).getHostAddress(); 
			
		} catch (Exception e) { 
			return ""; 
		}
	} 

	// change ip from String to int
	public static int ipToInt(String ipAddr) {
		try {
			 byte[] bytes = InetAddress.getByName(ipAddr).getAddress();
			 int addr = bytes[0] & 0xFF;
			 addr |= ((bytes[1] << 8 ) & 0xFF00);
			 addr |= ((bytes[2] << 16) & 0xFF0000);
			 addr |= ((bytes[3] << 24) & 0xFF000000);
	
			 Log.i("ipToInt" , ipAddr + "  to  " + addr );
			return  addr;
		} catch (Exception e) {
			throw new IllegalArgumentException(ipAddr + " is invalid IP");
		}
	}
	
	// slice ip to 4 pieces ,e.g. 192.168.43.1 to [ 192 , 168 , 43 , 1 ]
	public static int[] sliceIP( String IP ){
		String[] temp = IP.split(".");
		int[] result = new int[4];
		for( int i = 0 ; i < 4 ; i ++ )
			result[i] = Integer.valueOf( temp[i] );
		return result;
	}
	
	public static int getAndroidSDKVersion() { 
	   int version = 0; 
	   try { 
	     version = Integer.valueOf(android.os.Build.VERSION.SDK_INT); 
	   } catch (NumberFormatException e) { 
		   e.printStackTrace();
	   } 
	   return version; 
	}
	
	/**
	 *  pack the message by the protocol,
	 *  protocol:
	 *  	 IP   |   Type   |   Message
	 *     4bytes	 2bytes	   1024bytes MAX
	 * @param destination
	 * @param type
	 * @param message
	 * @return
	 */
	public static byte[] combineMessage( int destination , String type , String message ){
		Log.v("MessageTest" , destination + " " + type + " " + message );
		
		byte[] temp = message.getBytes();
		
		byte[] result = new byte[ Constant.BUFF_IP + 
		                          Constant.BUFF_TYPE + temp.length ];
		result[3] = (byte) (destination & 0xff);  
		result[2] = (byte) (destination >> 8 & 0xff);  
		result[1] = (byte) (destination >> 16 & 0xff);  
		result[0] = (byte) (destination >> 24 & 0xff);
		
		Integer t1 = Integer.valueOf(type.charAt(0));
		Integer t2 = Integer.valueOf(type.charAt(1));
		
		result[4] = t1.byteValue();
		result[5] = t2.byteValue();
		
		Log.v("Test",  result[4] + "" + result[5] );
		
		
		Log.v( "Test" , temp.length + "" );
		for( int i = 0 ; i < temp.length ; i ++ ){
			result[6 + i] = temp[i];
			System.out.println( temp[i] );
		}
		
		Log.v( "Message", result[0] +""+ result[1] + "" + result[2] +""+result[3] );
		Log.v( "Message", result[4] +""+ result[5]);
		Log.v( "Message", result.toString() + " " + result.length );
		
		return result;
	}
	
	public static byte[] combineMessage( int destination , String type , byte[] message ){
		
		byte[] result = new byte[ Constant.BUFF_IP + 
		                          Constant.BUFF_TYPE + message.length ];
		result[3] = (byte) (destination & 0xff);  
		result[2] = (byte) (destination >> 8 & 0xff);  
		result[1] = (byte) (destination >> 16 & 0xff);  
		result[0] = (byte) (destination >> 24 & 0xff);
		
		Integer t1 = Integer.valueOf(type.charAt(0));
		Integer t2 = Integer.valueOf(type.charAt(1));
		
		result[4] = t1.byteValue();
		result[5] = t2.byteValue();
		
		Log.v("Test",  result[4] + "" + result[5] );
		
		
		Log.v( "Test" , message.length + "" );
		for( int i = 0 ; i < message.length ; i ++ ){
			result[6 + i] = message[i];
			System.out.println( message[i] );
		}
		
		return result;
	}
	
	/**
	 *  unpack message into objects
	 *  protocol:
	 *  	 IP   |   Type   |   Message
	 *     4bytes	 2bytes	   1024bytes MAX
	 * @param buffer
	 * @param objects
	 */
	public static void handleMessage( DefaultCharBuffer buffer , 
			Object[] objects ){
		
		byte[] temp1 = buffer.buffer[0];
		
		objects[0] = (temp1[3] & 0xff) | ((temp1[2] << 8) & 0xff00) // | 表示安位或 
		 | ((temp1[1] << 24) >>> 8) | (temp1[0] << 24); 
		
		Log.v("handleMessage" , "Source IP: " + Method.ipIntToString((Integer)objects[0]));
		
		objects[1] = new String( buffer.buffer[1] );
		
		Log.v("handleMessage" , "Type: " + (String)objects[1] );
		
		objects[2] = buffer.buffer[2] ;
		
		Log.v("handleMessage" , "Message: " + new String( buffer.buffer[2] ) );
		
		
	}
	
	/**
	 *  analyse User list message
	 *  	IP_1||name_1||type_1|IP_2||name_2||type_2|
	 *  user is separated by '|' , user info is separated by '||'
	 * @param userList
	 * 		map client's IP(Integer) to User info
	 * @param message
	 * 	message from server
	 */
	public static void analyseUserinfo( Map<Integer , User> userList , String message ){
		
		userList.clear();
		
		String[] infoList = message.split("\\|\\|");
		Log.v("Method" , infoList.length + "" );
		
		for( int i = 0 ; i < infoList.length ; i ++ ){
			String[] info = infoList[i].split("\\|");
			int IP = Integer.valueOf( info[0] );
			String name = info[1];
			int type = Integer.valueOf( info[2] );
			
			Log.v("analyseUserinfo" , "IP = " + Method.ipIntToString(IP) );
			Log.v("analyseUserinfo" , "name = " + name );
			Log.v("analyseUserinfo" , "type = " + type );
			
			userList.put(IP, new User( IP , name , type ));
		}
		
		Log.v("analyseUserinfo" , "UserList size = " + userList.size() );
	}

	/**
	 * scale View to suit to screen
	 * @param view
	 */
	public static void scaleView( View view ){
		LayoutParams para;  
        para = view.getLayoutParams(); 
        Log.d("ScaleView" , "Before scale: " + para.height + " " + para.width );
        para.height = (int)( view.getLayoutParams().height * Constant.SCALE + 0.5f) ;
        para.width = (int)( view.getLayoutParams().width * Constant.SCALE + 0.5f);
        Log.d("ScaleView" , "After scale: " + para.height + " " + para.width );
        view.setLayoutParams(para);
	}
}
