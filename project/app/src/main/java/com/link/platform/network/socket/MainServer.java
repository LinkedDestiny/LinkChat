package com.link.platform.network.socket;

import android.util.Log;

import com.link.platform.util.*;
import com.link.platform.util.Error;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private IController controller;
    private ByteBuffer buffer;

    private Thread thread;

    public MainServer( int port, IController controller) {
        this.port = port;
        this.controller = controller;

        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE);
        connect_list = new ArrayList<Socket>();

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
            thread.start();
            loop = true;
        } else {
            pause = false;
        }

    }

    public void pause() {
        pause = true;
    }

    public void stop() {
        loop = false;
    }

    public List<Socket> getConnect_list() {
        return connect_list;
    }

    @Override
    public void run() {
        try{
            while ( loop ) {
                if ( !pause ) {
                    select();
                }
            }
            close();
        }catch( IOException e ){
            e.printStackTrace();
            try {
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    public int send(Socket fd, String message) throws IOException {
        SocketChannel channel = fd.getChannel();
        if( channel == null )
            return Error.IO_NO_CHANNEL;

        ByteBuffer temp = ByteBuffer.wrap(message.getBytes());

        long length = channel.write(temp);
        if( length > 0 ) {
            return Error.IO_SUCCESS;
        } else {
            return Error.IO_FAILURE;
        }

    }

    private void close() throws IOException {
        server.close();
        mainchannel.close();
        selector.close();
        buffer.clear();

        server = null;
        mainchannel = null;
        selector = null;
        buffer = null;
    }

    private void select() throws IOException {
        selector.select();

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

            // TODO 包完整性校验
            int errno = IOHelper.read(channel, buffer);

            if( errno == Error.IO_CLOSE ) {
                controller.onClose(channel.socket());
                connect_list.remove(channel.socket());
                channel.close();

            } else {
                controller.onReceive(channel.socket(), buffer.asCharBuffer().toString());

                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
            }
        }
    }
}
