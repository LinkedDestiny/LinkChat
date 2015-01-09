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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
    private List<ByteBuffer> dataList;
    private IClient iClient;

    private Thread thread;
    private Thread write_thread;

    static int index = 0;

    public MainClient( String host, int port, IClient iClient) {
        this.host = host;
        this.port = port;
        this.iClient = iClient;

        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE);
        loop = false;
        dataList = Collections.synchronizedList(new LinkedList<ByteBuffer>());
    }

    public void start() {
        if (!loop) {
            thread = new Thread(this);
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
                                    ByteBuffer buff = dataList.remove(0);
                                    if( buff == null ) {
                                        continue;
                                    }
                                    Log.d(TAG, "buffer size = " + buff.array().length);
                                    Log.d(TAG, "send " + index + "th pachage: size = " + buff.getInt(0));
                                    if( mainchannel != null && mainchannel.isOpen() ) {
                                        length = mainchannel.write(buff);
                                    }
                                    index ++;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if( length < 0 ) {
                                iClient.onError((int)length);
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
        dataList.add(temp);
        return Error.IO_SUCCESS;
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

            SocketChannel channel = (SocketChannel) key.channel();

            int errno = IOHelper.read(channel, buffer);
            if( errno == Error.IO_CLOSE ) {
                buffer.clear();
                iClient.onError(errno);
                return;
            } else if( errno == Error.IO_FAILURE ) {
                buffer.clear();
                iClient.onError(errno);
                return;
            } else if( errno == Error.IO_PROTOCOL_NO_COMPLETE ) {
                return;
            } else {
                Log.d(TAG, "recv " + errno + " bytes");
                byte[] buff = new byte[errno];
                buffer.get( buff , 0 , errno );
                iClient.onReceive(ByteBuffer.wrap(buff));

                if( !buffer.hasRemaining() ) {
                    buffer.clear();
                } else {
                    while( buffer.remaining() > 4 ) {
                        int len = buffer.getInt();
                        Log.d(TAG, "recv " + len + " bytes");
                        if( len > Utils.BUFFER_SIZE || len < 0 ) {
                            buffer.clear();
                            break;
                        }
                        else if( buffer.remaining() < len ) {
                            buffer.position( buffer.position() - 4 );
                            break;
                        } else {
                            byte[] buffs = new byte[len];
                            buffer.get( buffs , 0 , len );
                            iClient.onReceive(ByteBuffer.wrap(buffs));
                        }
                    }
                    buffer.compact();
                }
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }
}
