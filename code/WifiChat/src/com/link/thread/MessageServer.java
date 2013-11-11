package com.link.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

import com.link.ChatroomActivity;
import com.link.info.ChannelInfo;
import com.link.info.DefaultCharBuffer;
import com.link.info.User;
import com.link.listener.ServerTransferListener;
import com.link.thread.ServerThread.ServeTransferListener;
import com.link.tools.Method;
import com.link.util.Constant;

public class MessageServer extends MessageHandle{

	ChatroomActivity activity;
	private User server;
	
	private ServerThread thread;
	private Map< String , ChannelInfo > clients;
	
	private MessageTransferListener messageListener = new MessageTransferListener();
	private ServeTransferListener serverListener;
	
	private static String TAG = "MessageServer";
	
	public MessageServer( ChatroomActivity activity , int HostIP , String Username ){
		
		server = new User( HostIP , Username , User.USER_TYPE_SERVER );
		
		this.activity = activity;
		
		thread = new ServerThread( messageListener );
		
		clients = thread.getClientList();
		clients.put( Method.ipIntToString(HostIP),  new ChannelInfo( null , server ) );
		
		serverListener = thread.getTransferListener();
		
	}
	
	@Override
	public void startThread(){
		Log.d( TAG , "start ServerThraed");
		
		thread.listen();
		
		thread.start();
		
	}
	
	@Override
	public void stopThread() {
		
		thread.close();
	}
	
	@Override
	public void sendMessage( String message , int destination , String type ){
		
		sendMessage( server.getIP() , message , destination , type );
		
	}
	
	@Override
	public void sendVoice( String filename , int destination ) {
		
		sendVoice( server.getIP() , filename , destination );
	}
	
	private void sendVoice( int source , String filename , int destination ) {
		final int p_source = source;
		final String p_filename = filename;
		final int p_destination = destination;
		
		sendMessage( source , Constant.VOICE_BEGIN , destination , Constant.MESSAGE_SERVER_USER_VOICE );
		
		new Thread(){
			public void run(){
				File file = new File( p_filename );
				
				if( !file.exists() ){
					Log.e(TAG, " The file " + p_filename + " doesn't exists");
					return;
				}
				
				byte[] buffer = new byte[ Constant.BUFF_MESSAGE ];
				try {
					FileInputStream input = new FileInputStream( file );
					int num;
					while( ( num = input.read(buffer) ) > 0 ){
						
						if( num <= 0 )
							break;
						
						byte[] buff = Method.combineMessage( p_source, Constant.MESSAGE_SERVER_USER_VOICE, buffer );
						if( p_destination < 0 ) {
							Log.d(TAG , "send voice to all user" );
							
							synchronized( clients ){
								Iterator<ChannelInfo> iter = clients.values().iterator();
								
								while( iter.hasNext() ){
									ChannelInfo temp = ( ChannelInfo )iter.next();
									
									if( temp.user.getIP() == p_source || temp.channel == null )
										continue;
									
									serverListener.Write( temp.channel , buff );
								}
							}
							
						} else {
							
							String IP = Method.ipIntToString(p_destination);
							Log.d(TAG , "send voice to " + IP );
							
							serverListener.Write( clients.get(IP).channel , buff );
						}
					}
					
					input.close();
					
					
				} catch (IOException e) {
					Log.e(TAG , "Open voice file InputStream failed ");
					e.printStackTrace();
				}
				
				sendMessage( p_source , Constant.VOICE_END, p_destination , Constant.MESSAGE_SERVER_USER_VOICE );
			}
			
		}.start();
		
	}
	
	private void sendMessage( int source , String message , int destination , String type ){
		
		byte[] buff = null;
		if( type.equals( Constant.MESSAGE ) )
			buff = Method.combineMessage( source ,
				Constant.MESSAGE_SERVER_USER_MESSAGE , message );
		else{
			buff = Method.combineMessage( source ,
					type , message );
		}
		
		if ( destination < 0 ) {
			Log.d(TAG , "send message to all user" );

			synchronized( clients ){
				Iterator<ChannelInfo> iter = clients.values().iterator();
				
				while( iter.hasNext() ){
					ChannelInfo temp = ( ChannelInfo )iter.next();
					
					if( temp.user.getIP() == source || temp.channel == null )
						continue;
					
					serverListener.Write( temp.channel , buff );
				}
			}
		
		} else {
			
			String IP = Method.ipIntToString(destination);
			Log.d(TAG , "send message to " + IP );
			
			serverListener.Write( clients.get(IP).channel , buff );
			
		}
		
	}
	
	/**
	 * send new clients list to all client
	 */
	private void sendClientList(){
		Log.d( TAG , "send ClientList " + clients.size() );
		
		String message = "";
		// contain the Client Info , use "|" and "||" as decollator
		synchronized( clients ){
			Iterator<ChannelInfo> iter = clients.values().iterator();
			
			while( iter.hasNext() ){
				ChannelInfo temp = ( ChannelInfo )iter.next();
				message += ( temp.user.getIP() + "|" + temp.user.getUserName() 
						+ "|" + temp.user.getUserType() + "||" );
			}
			
			String type = Constant.MESSAGE_SERVER_UPDATE_USER_LIST;
			
			iter = clients.values().iterator();
			
			while( iter.hasNext() ){
				ChannelInfo temp = ( ChannelInfo )iter.next();
				
				if( temp.channel == null )
					continue;
				
				Log.v( TAG , Method.ipIntToString(temp.user.getIP()) );
				
				byte[] buff = Method.combineMessage( server.getIP() , type, message );
				
				serverListener.Write( temp.channel , buff );
			}
		}
	}
	
	/**
	 * 
	 * @param source
	 * @param objects
	 */
	private void operate( String source , Object[] objects ){
		
		String type = ( String ) objects[1];
		
		Log.d(TAG , "Server operate type: " + type );
		
		if ( type.equals( Constant.MESSAGE_CLIENT_USERINFO ) ){
			Log.i( TAG , "Message_client_userinfo: " + (String)objects[2] );
			
			ChannelInfo user = clients.get( source );
			
			if( user == null )
				return;
			// set user name
			user.user = new User( Method.ipToInt(source) , new String( (byte[]) objects[2] ), User.USER_TYPE_CLIENT );
			
			// update the clientList
			sendClientList();
			
		} else if ( type.equals( Constant.MESSAGE_CLIENT_MESSAGE ) ){
			Log.i(TAG , "Message_client_message");
			
			Map< String , Object > newMessage = new HashMap<String , Object>();
			newMessage.put("Username", clients.get(source).user.getUserName() );
			newMessage.put("Usermessage", new String( (byte[]) objects[2] ) );
			
			// post the message to activity
			activity.updateMessageList(newMessage);
			
			sendMessage( Method.ipToInt(source) , new String( (byte[]) objects[2] ) , (Integer)objects[0] , Constant.MESSAGE );
			
		} else if ( type.equals( Constant.MESSAGE_CLIENT_VOICE ) ) {
			
			
		}
		
	}
	 
	
	public class MessageTransferListener implements ServerTransferListener{

		@Override
		public void Read( String source , DefaultCharBuffer buff ) {
			
			Object[] objects = new Object[3];
			
			// split buff into message
			Method.handleMessage( buff, objects );
			
			Log.v( TAG , "IP = " + Method.ipIntToString((Integer)objects[0]) );
			Log.v( TAG , "type = " + (String)objects[1] );
			Log.v( TAG , "message = " + (String)objects[2] );
			
			// analyse message and choose operation
			operate( source , objects );
		}

		@Override
		public void Write(SocketChannel channel, byte[] buff) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
