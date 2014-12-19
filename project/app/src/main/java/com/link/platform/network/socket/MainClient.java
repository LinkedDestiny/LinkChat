package com.link.platform.network.socket;

import android.util.Log;

import com.link.platform.util.*;
import com.link.platform.util.Error;
import com.link.platform.wifi.wifi.WiFiManager;

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
        loop = false;
    }

    public void start() {
        if (!loop) {
            thread = new Thread(this);
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
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws IOException {
        selector = Selector.open();
        mainchannel = SocketChannel.open();
        mainchannel.configureBlocking(true);
        socket = mainchannel.socket();
        // connect to the HostIP

        mainchannel.configureBlocking(false);
        mainchannel.register(selector, SelectionKey.OP_CONNECT);
        mainchannel.connect(new InetSocketAddress( host , port ));
    }

    public int send(byte[] message) throws IOException {

        ByteBuffer temp = ByteBuffer.allocate( 4 + message.length );
        temp.putInt( message.length );
        temp.put( message );
        temp.flip();
        long length = mainchannel.write(temp);
        if( length > 0 ) {
            return Error.IO_SUCCESS;
        } else {
            return (int)length;
        }

    }

    @Override
    public void run() {
        try{
            connect();
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
        Log.d(TAG, "Close client");
    }

    private void close() throws IOException {
        if( socket != null )
            socket.close();

        if( mainchannel != null )
            mainchannel.close();

        if( selector != null )
            selector.close();

        if( buffer != null )
            buffer.clear();

        socket = null;
        mainchannel = null;
        selector = null;
        buffer = null;
    }

    private void select() throws IOException {
        selector.select( 30 * 1000 );

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
        if( key.isConnectable() ) {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.finishConnect()) {
                int num_ip = WiFiManager.getInstance().getIP();
                if( num_ip == 0 ) {
                    iClient.onConnect("127.0.0.1");
                }
                else {
                    iClient.onConnect(IOHelper.ipIntToString(num_ip));
                }


                mainchannel.configureBlocking(false);
                mainchannel.register(selector, SelectionKey.OP_READ );
            } else {
                iClient.onConnect("");
            }
        }
        if (key.isReadable()) {
            Log.d(TAG, "SelectionKey Reading ...");

            SocketChannel channel = (SocketChannel) key.channel();

            // TODO 包完整性校验
            int errno = IOHelper.read(channel, buffer);
            if( errno == Error.IO_CLOSE ) {
                iClient.onError(errno);
                return;
            } else if( errno == Error.IO_FAILURE ) {
                iClient.onError(errno);
                return;
            } else if( errno == Error.IO_PROTOCOL_NO_COMPLETE ) {
                return;
            } else if( errno > 0 ) {
                byte[] buff = new byte[errno];
                buffer.get( buff );
                iClient.onReceive(ByteBuffer.wrap(buff));

                if( !buffer.hasRemaining() ) {
                    buffer.clear();
                } else {
                    buffer.compact();
                }
            } else {

            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }
}
