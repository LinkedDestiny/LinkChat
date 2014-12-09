package com.link.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.link.info.ChannelInfo;
import com.link.info.DefaultBuffer;
import com.link.info.DefaultCharBuffer;
import com.link.listener.ServerTransferListener;
import com.link.tools.SocketMethod;
import com.link.util.Constant;

public class ServerThread extends BaseThread{

	private boolean loop = true;
	private boolean pause = false;
	
	
	private ServerSocketChannel mainchannel;		// server socket channel
	private ServerSocket server;					// server socket
	private Selector selector;
	private DefaultBuffer buffer;
	private DefaultCharBuffer readline;
	private byte[] writeBuffer;
	
	private Map< String , ChannelInfo > clients;	// list of clients
	
	private ServerKeyHandler keyHandler;
	private ServerTransferListener listener;		// transfer message from server to activity
	private ServeTransferListener serveListener;	// get message from activity
	
	private static String TAG = "ServerThread";
	
	
	public ServerThread( ServerTransferListener listener ){
		
		clients = new HashMap< String , ChannelInfo >();
		mainchannel = null;
		this.listener = listener;
		
		buffer = new DefaultBuffer();
		readline = new DefaultCharBuffer();
		writeBuffer = new byte[ Constant.BUFFER_SIZE + 6 ];
		
		keyHandler = new ServerKeyHandler();
		serveListener = new ServeTransferListener();
		
		try{
			selector = Selector.open();
		}catch( IOException e ){
			Log.e( TAG , "selector open failed" );
			e.printStackTrace();
		}
	}
	
	public Map< String , ChannelInfo > getClientList(){
		return clients;
	}
	
	public ServeTransferListener getTransferListener(){
		return serveListener;
	}
	
	/**
	 * listen to the port
	 */
	public void listen(){
		
		try {
			mainchannel = SocketMethod.listen( Constant.SERVER_PORT, mainchannel, server );
			mainchannel.configureBlocking(false);
			
			// register accept event
			mainchannel.register(selector, SelectionKey.OP_ACCEPT );
			
		} catch (IOException e) {
			Log.e( TAG , "listen failed");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Main loop
	 */
	public void run(){
		
		try{
			while ( loop ) {
				if ( !pause ) {
					SocketMethod.select( selector , keyHandler );
				}
			}
		}catch( IOException e ){
			e.printStackTrace();
		}
	}
	
	public void close(){
		
		try {
			
			loop = false;
			
			selector.close();
			server.close();
			mainchannel.close();
			clients.clear();
			
			selector = null;
			server = null;
			mainchannel = null;
			clients = null;
			
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
	}
	
	private class ServerKeyHandler implements SocketMethod.KeyHandler{

		@Override
		public void Accept(SocketChannel channel) {
			
			try {
				
				String IP = channel.socket().getInetAddress().getHostAddress();
				
				clients.put( IP ,  new ChannelInfo( channel , null ));
				
				Log.i(TAG , "new Client " + IP + " || Clients size = " + clients.size() );
				
				channel.configureBlocking(false);
				channel.register(selector, SelectionKey.OP_READ );
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void Read(SocketChannel channel) {
			try {
				Log.d(TAG , "ServerKeyHandler  reading");
				
				
				int flag = SocketMethod.Read( channel , buffer, readline );
				
				if ( flag == Constant.IO_NO_CHANNEL ) {
					
					Log.e( TAG , " channel not exist" );
					return;
					
				} else if ( flag == Constant.IO_FAILURE ) {
					Log.e( TAG , " serve closed connection " );
					
					return;
				} else if ( flag == Constant.IO_SUCCESS ) {
					Log.v( TAG , " read message ");
					
					listener.Read( channel.socket().getInetAddress().getHostAddress() , readline );
					
					SocketMethod.setChannelOperattion(channel, 
							SelectionKey.OP_READ, selector, false );
				}
				
			} catch (IOException e) {
				Log.e( TAG , " read failed" );
				e.printStackTrace();
			}
		}

		@Override
		public void Write(SocketChannel channel) {
			try {
				Log.d(TAG , "ServerKeyHandler  writing");
				SocketMethod.Write(channel, writeBuffer);
				
				SocketMethod.setChannelOperattion(channel, 
						SelectionKey.OP_READ, selector, false );
				
			} catch (IOException e) {
				Log.e( TAG , " write failed" );
				e.printStackTrace();
			}
		}
		
	}
	
	public class ServeTransferListener implements ServerTransferListener{

		@Override
		public void Read( String source , DefaultCharBuffer buff ) {}

		@Override
		public void Write( SocketChannel channel , byte[] buff ) {
			Log.d(TAG , "ServeTransferListener Writing..." );
			writeBuffer = buff;
			
			//SocketMethod.setChannelOperattion( channel , 
			//		SelectionKey.OP_WRITE, selector, true );
			
			keyHandler.Write(channel);
		}
		
	}
}
