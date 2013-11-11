package com.link.thread;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import android.util.Log;

import com.link.info.DefaultBuffer;
import com.link.info.DefaultCharBuffer;
import com.link.listener.TransferListener;
import com.link.tools.SocketMethod;
import com.link.util.Constant;

public class ClientThread extends BaseThread{
	
	private boolean loop = true;
	private boolean pause = false;
	
	private SocketChannel mainchannel;				// main socket channel
	private Socket client;							// socket connected to the server
	private Selector selector;						// I/O selector
	private DefaultBuffer buffer;					// I/O buffer
	private byte[] writeBuffer;						
	private DefaultCharBuffer readline;
	
	private ClientKeyHandler keyHandler;			
	private TransferListener transferlistener;		// transfer message from client to activity
	private ClientTransferListener clientListener;	// get message from activity
	private String HostIP;					
	
	private static String TAG = "ClientThread";
	
	public ClientThread( TransferListener listener ){
		this.transferlistener = listener;
		
		mainchannel = null;
		keyHandler = new ClientKeyHandler();
		clientListener = new ClientTransferListener();
		
		buffer = new DefaultBuffer();
		readline = new DefaultCharBuffer();
		
		try{
			selector = Selector.open();
		}catch( IOException e ){
			Log.e( TAG , "selector open failed" );
			e.printStackTrace();
		}
	}
	
	
	
	public void setHostIP( String HostIP ){
		this.HostIP = HostIP;
	}
	
	/**
	 * send Listener to activity
	 * @return
	 * 		ClientTransferListener
	 */
	public ClientTransferListener getTransferListener(){
		return this.clientListener;
	}
	
	/**
	 * connect to the server
	 */
	public void connect(){
		try {
			// connnect to the Host server
			mainchannel = SocketMethod.connect(HostIP, Constant.CLIENT_PORT, mainchannel, client );
			
			// set asynchronous communication
			mainchannel.configureBlocking(false);
			
			// register read event
			mainchannel.register(selector, SelectionKey.OP_READ );
			
		} catch (IOException e) {
			Log.e( TAG , "connect failed");
			e.printStackTrace();
		}
		Log.d(TAG , "Connect success" + ( mainchannel == null ));
	}
	
	/**
	 * Main loop
	 */
	public void run(){
		
		try {
			
			while( loop ){
				if( !pause ){
					// handle the selector
					SocketMethod.select( selector , keyHandler );
				}
			}
			
		} catch (IOException e) {
			Log.e( TAG , "select failed");
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		
		try {
			
			loop = false;
			
			selector.close();
			client.close();
			mainchannel.close();
			
			selector = null;
			client = null;
			mainchannel = null;
			
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	public class ClientTransferListener implements TransferListener{

		@Override
		public void Read(DefaultCharBuffer buff ) {}

		@Override
		public void Write( byte[] buff ) {
			
			// get the message
			writeBuffer = buff;
			
			// register write event
			SocketMethod.setChannelOperattion(mainchannel, 
					SelectionKey.OP_WRITE, selector, true);
			
		}
		
	}
	
	public class ClientKeyHandler implements SocketMethod.KeyHandler{

		private String TAG = "ClientKeyHandler";
		@Override
		public void Accept(SocketChannel channel) {}

		@Override
		public void Read(SocketChannel channel) {
			try {
				Log.d(TAG , "ClientkeyHandler  reading");
				
				// read message from the server
				int flag = SocketMethod.Read( channel, buffer, readline );
				
				if ( flag == Constant.IO_NO_CHANNEL ) {
					
					Log.e( TAG , " channel not exist" );
					return;
					
				} else if ( flag == Constant.IO_FAILURE ) {
					Log.e( TAG , " serve closed connection " );
					
					// TODO 
					// reconnect or close client
					
					return;
				} else if ( flag == Constant.IO_SUCCESS ) {
					Log.v( TAG , " read message: " + readline.toString() );
					
					// send message to activity
					transferlistener.Read( readline );
					
					// register read event
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
				Log.d( TAG , "WriteLength = " + writeBuffer.length +"" );
				SocketMethod.Write(mainchannel, writeBuffer);
				
				SocketMethod.setChannelOperattion(channel, 
						SelectionKey.OP_READ, selector, false );
				
			} catch (IOException e) {
				Log.e( TAG , " write failed" );
				e.printStackTrace();
			}
		}
		
	}

}
