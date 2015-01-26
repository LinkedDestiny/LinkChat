package com.link.platform.network.socket;

import android.util.Log;

import com.link.platform.network.SendItem;
import com.link.platform.util.*;
import com.link.platform.util.Error;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class MainServer implements Runnable {

    public final static String TAG = "MainServer";

    private boolean loop = false;
    private boolean pause = false;

    protected int port;
    private List<Socket> connect_list;

    private ServerSocketChannel mainchannel;		// server socket channel
    private ServerSocket server;					// server socket
    private Selector selector;
    private ByteBuffer buffer;

    private IController controller;

    private List<SendItem> dataList;
    private Thread thread;
    private Thread write_thread;

    public MainServer( int port, IController controller) {
        this.port = port;
        this.controller = controller;
        connect_list = new LinkedList<Socket>();

        dataList = Collections.synchronizedList(new LinkedList<SendItem>());
        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE);
    }

    public void listen() throws IOException {
        Log.d(TAG, "listening to the port  " + port + " ...");

        selector = Selector.open();

        mainchannel = ServerSocketChannel.open();
        server = mainchannel.socket();
        server.bind( new InetSocketAddress( port ));

        mainchannel.configureBlocking(false);
        // register accept event
        mainchannel.register(selector, SelectionKey.OP_ACCEPT );
    }

    public void start() {
        if( !loop ) {
            thread = new Thread( this );
            loop = true;
            thread.start();
            write_thread = new Thread( new Runnable() {
                int index = 0;
                @Override
                public void run() {
                    while( loop ) {
                        while( dataList.size() > 0 ) {
                            long length = 0;
                            try {
                                if( dataList.size() > 0 ) {
                                    SendItem item = dataList.remove(0);
                                    if( item == null ) {
                                        continue;
                                    }
                                    SocketChannel channel = item.socket.getChannel();
                                    if( item.socket.isClosed() ) {
                                        continue;
                                    }
                                    if( channel == null )
                                        continue;
                                    Log.d(TAG, "buffer size = " + item.buffer.array().length);
                                    Log.d(TAG, "send " + index + "th pachage: size = " + item.buffer.getInt(0));

                                    length = channel.write(item.buffer);
                                    if( length < 0 ) {
                                        controller.onClose(item.socket);
                                    } else {
                                        index ++;
                                    }

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            write_thread.start();
        } else {
            pause = false;
        }

    }

    public void pause() {
        pause = true;
    }

    public void stop() {
        loop = false;
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Socket> getConnect_list() {
        return connect_list;
    }

    @Override
    public void run() {
        try{
            while ( loop ) {
                if ( !pause ) {
                    Log.d(TAG, "Main loop: select...");
                    select();
                }
            }
            //close();
        }catch( IOException e ){
            e.printStackTrace();
            try {
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        Log.d(TAG, "close server thread");
    }

    public void send(Socket fd, byte[] message) throws IOException {
        Log.d(TAG, "message length = " + message.length );
        ByteBuffer temp = ByteBuffer.allocate( 4 + message.length );
        temp.putInt( message.length );
        temp.put( message, 0, message.length );
        temp.flip();
        SendItem item = new SendItem(fd, temp);
        synchronized (dataList) {
            dataList.add(item);
        }

    }

    private void close() throws IOException {
        if( server != null )
            server.close();

        if( mainchannel != null )
            mainchannel.close();

        if( selector != null )
            selector.close();

        server = null;
        mainchannel = null;
        selector = null;
    }

    private void select() throws IOException {
        selector.select( Utils.SELECT_TIMEOUT );

        if( loop && !pause ) {
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            while( iter.hasNext() ){
                SelectionKey key = iter.next();
                iter.remove();

                if( key.isValid() ) {
                    handleSelectionKey( key , selector );
                }
            }
        }
    }

    public void handleSelectionKey( SelectionKey key, Selector selector ) throws IOException {

        Log.d(TAG, "handleSelectionKey ");

        if (key.isAcceptable()) {
            Log.d(TAG, "SelectionKey Accepting ...");

            //get the server channel
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            //accept the connection of client
            SocketChannel channel = server.accept();

            // if accept failed , throw Exception
            if (channel == null) {
                throw new IOException("accpet failed");
            }
            connect_list.add(channel.socket());
            controller.onConnect(channel.socket());

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);

            // set the channel non-blocking
            channel.configureBlocking(false);

        } else if (key.isReadable()) {
            Log.d(TAG, "SelectionKey Reading ...");

            SocketChannel channel = (SocketChannel) key.channel();


            int count = channel.read(buffer);
            if( count < 0 ) {   // close connection
                controller.onClose(channel.socket());
                connect_list.remove(channel.socket());
                channel.close();
                return;
            }
            else if( count == 0 ) { // no data or buffer is full

            }
            else {
                Log.d(TAG, "read bytes : " + count );
                buffer.flip();
                controller.onReceive(channel.socket(), buffer);
            }
            buffer.clear();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }
}
