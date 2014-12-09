package com.link.thread;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.link.ChatroomActivity;
import com.link.info.DefaultCharBuffer;
import com.link.info.User;
import com.link.listener.TransferListener;
import com.link.thread.ClientThread.ClientTransferListener;
import com.link.tools.Method;
import com.link.util.Constant;

public class MessageClient extends MessageHandle{
	
	ChatroomActivity activity;
	private User client;
	private int HostIP;
	private ClientThread thread;
	private MessageTransferListener messageListener;
	private ClientTransferListener clientListener;
	
	private Map< Integer , User > userList;
	
	private static String TAG = "MessageClient";
	
	public MessageClient( ChatroomActivity activity , int HostIP , String Username , int IP ){
		client = new User( IP , Username , User.USER_TYPE_CLIENT );
		Log.v(TAG , "IP = " + IP );
		this.HostIP = HostIP;
		this.activity = activity;
		
		userList = new HashMap< Integer , User >();
		
		messageListener = new MessageTransferListener();
		
		thread = new ClientThread( messageListener );
		clientListener = thread.getTransferListener();
	}
	
	/**
	 * start the ClientThread
	 */
	public void startThread(){
		Log.d( TAG , "start ClientThraed");
		
		thread.setHostIP( Method.ipIntToString(HostIP) );
	
		thread.connect();
		
		sendClientInfo();
		
		thread.start();
		
	}
	
	public void stopThread() {
		
		thread.close();
	}
	
	private void sendClientInfo(){
		Log.v(TAG , "sendClientInfo :  " + client.getUserName() );
		
		byte[] buff = Method.combineMessage(client.getIP(), Constant.MESSAGE_CLIENT_USERINFO	
				, client.getUserName() );
		
		clientListener.Write(buff);
	}
	
	public void sendMessage( String message , int destination , String type ){
		Log.v(TAG , "sendMessage :  " + message + " to " +
				( (destination > 0 ) ? Method.ipIntToString(destination) : "All" ) );
		byte[] buff = null;
		if( type.equals(Constant.MESSAGE))
			buff = Method.combineMessage( destination , Constant.MESSAGE_CLIENT_MESSAGE	
				, message );
		else {
			buff = Method.combineMessage( destination , type
					, message );
		}
		
		clientListener.Write(buff);
	
	}
	
	private void operate( int source , Object[] objects ){
		
		String type = ( String ) objects[1];
		
		Log.d( TAG , "Client operate type: " + type );
		
		if ( type.equals( Constant.MESSAGE_SERVER_UPDATE_USER_LIST ) ) {
			
			Method.analyseUserinfo ( userList, (String)objects[2] );
			
		} else if ( type.equals( Constant.MESSAGE_SERVER_MESSAGE ) ) {
			
			
		} else if ( type.equals( Constant.MESSAGE_SERVER_USER_MESSAGE ) ) {
			Log.i(TAG , "Message_user_message");
			
			Map< String , Object > newMessage = new HashMap<String , Object>();
			newMessage.put("Username", userList.get(source).getUserName() );
			newMessage.put("Usermessage", objects[2] );
			
			// post the message to activity
			activity.updateMessageList(newMessage);
			
		}
	}
	
	public class MessageTransferListener implements TransferListener{

		private String TAG = "MessageTransferListener";
		@Override
		public void Read(DefaultCharBuffer buff) {
			Log.v(TAG , "read : " + buff.buffer[2].toString() );
			
			Object[] objects = new Object[3];
			
			Method.handleMessage( buff, objects );
			
			Log.v( TAG , "IP = " + Method.ipIntToString((Integer)objects[0]) );
			Log.v( TAG , "type = " + (String)objects[1] );
			Log.v( TAG , "message = " + (String)objects[2] );
			
			operate( (Integer)objects[0] , objects );
		}

		@Override
		public void Write(byte[] buff) {}
		
	}
}
