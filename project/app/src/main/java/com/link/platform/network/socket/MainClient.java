package com.link.platform.network.socket;

import android.util.Log;

import com.link.platform.util.*;
import com.link.platform.util.Error;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class MainClient implements Runnable {

    public final static String TAG = "MainClient";

    private boolean loop = true;
    private boolean pause = false;

    private String host;
    private int port;

    private SocketChannel mainchannel;				// main socket channel
    private Socket socket;							// socket connected to the server
    private Selector selector;

    private ByteBuffer buffer;
    private IClient iClient;

    private Thread thread;

    public MainClient( String host, int port, IClient iClient) {
        this.host = host;
        this.port = port;
        this.iClient = iClient;

        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE);

    }

    public void start() {
        if( !loop ) {
            thread = new Thread( this );
            loop = true;
            thread.start();
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

    public void connect() throws IOException {
        selector = Selector.open();
        mainchannel = SocketChannel.open();
        socket = mainchannel.socket();
        // connect to the HostIP
        socket.connect(new InetSocketAddress( host , port ));
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
            iClient.onError(Error.IO_FAILURE);
            e.printStackTrace();
            try {
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void close() throws IOException {
        socket.close();
        mainchannel.close();
        selector.close();
        buffer.clear();

        socket = null;
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
        if (key.isReadable()) {
            Log.d(TAG, "SelectionKey Reading ...");

            SocketChannel channel = (SocketChannel) key.channel();

            // TODO 包完整性校验
            int errno = IOHelper.read(channel, buffer);
            if( errno == Error.IO_CLOSE ) {

                return;
            } else if( errno == Error.IO_FAILURE ) {

                return;
            } else if( errno == Error.IO_PROTOCOL_NO_COMPLETE ) {

            } else {
                byte[] buff = new byte[errno];
                buffer.get( buff );
                iClient.onReceive(ByteBuffer.wrap(buff));

                if( !buffer.hasRemaining() ) {
                    buffer.clear();
                } else {
                    buffer.compact();
                }
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }
}
