package com.link.platform.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.link.platform.item.ContactItem;
import com.link.platform.network.item.ByteItem;
import com.link.platform.network.socket.IController;
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.socket.MainServer;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.Utils;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
abstract public class BaseServer implements IController {

    public final static String TAG = "BaseServer";

    protected MainServer server;
    protected Map<String, ContactItem> userList = new HashMap<String, ContactItem>();
    protected String host_ip = "192.168.43.1";

    protected Thread handlethread;
    protected boolean running = true;
    protected List<ByteItem> dataList;

    protected Map<Socket, ByteBuffer> buffer_map;

    protected HandlerThread handlerThread;
    protected Handler handler;

    public BaseServer(int port) {

        buffer_map = new HashMap<Socket, ByteBuffer>();

        server = new MainServer(port, this);
        try {
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataList = Collections.synchronizedList(new LinkedList<ByteItem>());
        handlethread = new Thread( new HandleRunnable() );
        handlerThread = new HandlerThread("serverThread");
    }

    public void start() {
        handlethread.start();
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        server.start();
    }

    public void pause() {
        server.pause();
    }

    public void stop() {
        server.stop();
        running = false;
    }

    abstract protected void handleMessage(ByteItem item, int arg1, int arg2, byte[] content);
    abstract protected void handleConnect(Socket fd);
    abstract protected void handleClose(Socket fd);


    @Override
    public void onConnect(Socket fd) {
        final Socket socket = fd;
        handler.post(new Runnable() {
            @Override
            public void run() {
                buffer_map.put(socket, ByteBuffer.allocate(Utils.BUFFER_SIZE * 2));
                handleConnect(socket);
            }
        });

    }

    @Override
    public void onReceive(Socket socket, ByteBuffer msg) {
        ByteItem item = new ByteItem(msg, socket);
        Log.d(TAG, "receive: " + item.buffer.length );
        synchronized (dataList) {
            dataList.add(item);
        }
    }

    @Override
    public void onClose(Socket fd) {
        final Socket socket = fd;

        handler.post(new Runnable() {
            @Override
            public void run() {
                handleConnect(socket);
            }
        });

    }

    class HandleRunnable implements Runnable {

        @Override
        public void run() {
            while( running ) {
                synchronized ( dataList ) {
                    while( dataList.size() > 0 ) {
                        ByteItem item = dataList.remove(0);
                        ByteBuffer buffer = buffer_map.get(item.socket);
                        Log.d(TAG, "buffer has :" + buffer.position() );
                        buffer.put(item.buffer);
                        buffer.flip();
                        while( buffer.remaining() > 4 ) {
                            int len = buffer.getInt();
                            Log.d(TAG, "read: " + len);
                            if( len < 0 || len > Utils.BUFFER_SIZE ) {
                                break;
                            }else if( buffer.remaining() < len ) {
                                buffer.position( buffer.position() - 4 );
                                break;
                            } else {
                                Log.d(TAG, "recv a package size = " + len);
                                byte[] buffs = new byte[len];
                                buffer.get( buffs , 0 , len );
                                int arg1 = IOHelper.bytesToInt(buffs, 0);
                                int arg2 = IOHelper.bytesToInt(buffs, 4);
                                byte[] content = new byte[ len - 8 ];
                                System.arraycopy(buffs, 8, content, 0, content.length);
                                handleMessage(item, arg1, arg2, content);

                            }
                        }
                        buffer.compact();
                        buffer_map.put(item.socket, buffer);
                    }
                }
            }
        }
    }
}
