package com.link.tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.link.info.DefaultBuffer;
import com.link.info.DefaultCharBuffer;
import com.link.util.Constant;

import android.util.Log;

public class SocketMethod {
	
	private static String TAG = "SocketMethod";
	
	/**
	 * select method
	 * @param selector
	 * @param handler	handle the operation of the SelectionKey
	 * @throws IOException
	 */
	public static void select( Selector selector , KeyHandler handler ) 
			throws IOException{
		
		selector.select();
		
		Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
		
		while( iter.hasNext() ){
			
			SelectionKey key = ( SelectionKey )iter.next();
			iter.remove();
			
			if( key.isValid() )
				handleSelectionKey( key , handler , selector );
		}
		
	}
	/**
	 * connect to the HostIP
	 * @param HostIP	IPAddress of Host
	 * @param port 		port of the server
	 * @param socketchannel		SocketChannel of the Selector
	 * @param socket	connect socket
	 * @throws IOException 
	 */
	public static SocketChannel connect( String HostIP , int port ,
			SocketChannel socketchannel , Socket socket ) throws IOException{
		Log.d( TAG , "connecting to " + HostIP +  " .  Port is " + port + " ..." );
		
		// open the channel
		socketchannel = SocketChannel.open();
		// get the channel's socket
		socket = socketchannel.socket();
		// connect to the HostIP
		socket.connect(new InetSocketAddress( HostIP , port ));
		
		return socketchannel;
	}
	
	/**
	 * listen the port
	 * @param port		
	 * @param channel	SocketChannel of the Selector
	 * @param socket	listen socket
	 * @throws IOException
	 * 		channel open fail
	 * 		socket bind fail
	 */
	public static ServerSocketChannel listen( int port , ServerSocketChannel channel , 
			ServerSocket socket ) throws IOException{
		Log.d( TAG , "listening to the port  " + port + " ..." );
		
		// open the ServerSocketChannel
		channel = ServerSocketChannel.open();
		// get the ServerSocket
		socket = channel.socket();
		// bind the port with the socket
		socket.bind( new InetSocketAddress( port ));
		
		return channel;
	}
	
	public static void handleSelectionKey( SelectionKey key , 
			KeyHandler handler , Selector selector ) throws IOException{
		
		Log.d( TAG , "handleSelectionKey " );
		
		if ( key.isAcceptable() ) {
			Log.d( TAG , "SelectionKey Accepting ...");
			
			//get the server channel
			ServerSocketChannel server = ( ServerSocketChannel )key.channel();
			//accept the connection of client
			SocketChannel channel = server.accept();
			
			// if accept failed , throw Exception
			if( channel == null ){
				throw new IOException("accpet failed");
			}
			
			handler.Accept(channel);
			
			// set the channel non-blocking
			channel.configureBlocking(false);
			
		} else if ( key.isReadable() ) {
			Log.d( TAG , "SelectionKey Reading ...");
			
			SocketChannel channel = ( SocketChannel ) key.channel();
			
			handler.Read(channel);
				
		} else if ( key.isValid() && key.isWritable() ) {
			Log.d( TAG , "SelectionKey Writing ...");
			
			SocketChannel channel = ( SocketChannel ) key.channel();
			
			handler.Write(channel);
			
		}
		
	}
	
	/**
	 * change the operator of the Channel
	 * @param channel	
	 * @param operation	the new operator of the channel
	 * @param selector	the selector which the channel belongs
	 * @param wakeup    true : wakeup the selector immediately ; otherwise not
	 */
	public static void setChannelOperattion( SocketChannel channel , 
			int operation , Selector selector , boolean wakeup ){
		Log.d( TAG , "setChannelOperattion " + operation + "... ");
		
		// get the key of the channel
		SelectionKey key = channel.keyFor(selector);
		// set the new operation
		key.interestOps(operation);
		
		// if wakeup, do the operation immediately; 
		// if not, channel will wait the old operation
		if( wakeup ){
			Log.d(TAG , "selector wakeup ");
			selector.wakeup();
			Log.d(TAG , key.isWritable() + "" );
		}
	}
	
	/**
	 * encapsulation of read
	 * @param channel	read from the channel
	 * @param buffer	
	 * @param readline	the data read from the channel
	 * @return 
	 * @throws IOException
	 */
	public static int Read( SocketChannel channel , DefaultBuffer buffer 
			, DefaultCharBuffer readline ) 
			throws IOException{
		Log.d( TAG , "Reading ... ");
		
		
		if( channel == null )
			return Constant.IO_NO_CHANNEL;
		
		long count = channel.read(buffer.buffer);
		if ( count > 0 ) {
			Log.d("Read" , count + "" );
			buffer.flip();

			readline.setBuffer(buffer.buffer , count );
			
			buffer.clear();
			
			return Constant.IO_SUCCESS;
		} else {
			channel.close();
			return Constant.IO_FAILURE;
		}
	}
	
	/**
	 * encapsulation of write
	 * @param channel
	 * @param writeline
	 * @return
	 * @throws IOException
	 */
	public static int Write( SocketChannel channel , byte[] writeline ) 
			throws IOException{
		
		Log.d( TAG , "Writing ... ");
		
		if( channel == null )
			return Constant.IO_NO_CHANNEL;
		
		DefaultBuffer temp = new DefaultBuffer( writeline );
		
		long length = channel.write(temp.buffer);
		Log.d( TAG , "Write : " + length );
		
		return Constant.IO_SUCCESS;
	}
	
	// the handle functions of the SelectionKey
	public interface KeyHandler{
		
		public void Accept( SocketChannel channel );
		
		public void Read( SocketChannel channel );
		
		public void Write( SocketChannel channel );
	}
}
