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
public class BaseController implements IController {

    public final static String TAG = "BaseController";

    private MainServer server;
    private Map<String, ContactItem> userList = new HashMap<String, ContactItem>();
    private String host_ip = "192.168.43.1";

    public int index = 0;
    private Thread handlethread;
    private boolean running = true;
    private List<ByteItem> dataList;

    private Map<Socket, ByteBuffer> buffer_map;

    private HandlerThread handlerThread;
    private Handler handler;

    public BaseController(int port) {

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


    @Override
    public void onConnect(Socket fd) {
        final Socket socket = fd;
        handler.post(new Runnable() {
            @Override
            public void run() {
                buffer_map.put(socket, ByteBuffer.allocate(Utils.BUFFER_SIZE * 2));
                List<Socket> connect_list = server.getConnect_list();
                Iterator<Socket> iter = connect_list.iterator();
                while( iter.hasNext() ) {
                    Socket socket = iter.next();
                    Log.d(TAG, socket.getInetAddress().getHostAddress() );
                }
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
                if( socket == null || socket.getInetAddress() == null ) {
                    return;
                }
                Log.e(TAG, socket.getInetAddress().getHostAddress() + " close connection");
                ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_OFFLINE, host_ip , socket.getInetAddress().getHostAddress().getBytes());
                List<Socket> connect_list = server.getConnect_list();
                Iterator<Socket> iter = connect_list.iterator();
                while( iter.hasNext() ) {
                    Socket socket = iter.next();
                    try {
                        server.send(socket, buffer.array());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                                int type = IOHelper.bytesToInt(buffs, 0);
                                String ip = IOHelper.ipIntToString( IOHelper.bytesToInt(buffs, 4));
                                byte[] buff = new byte[ len - 8 ];

                                List<Socket> connect_list = server.getConnect_list();
                                Iterator<Socket> iter = connect_list.iterator();

                                if( type == MsgType.MSG_ONLINE ) {
                                    index --;
                                    // send OnlineList
                                    String online_list = ProtocolFactory.parseOnlieList(userList);
                                    ByteBuffer send_msg = ProtocolFactory.parseProtocol(MsgType.MSG_ONLINE_LIST, host_ip , online_list.getBytes());
                                    try {
                                        server.send(item.socket, send_msg.array());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.arraycopy(buffs, 8, buff, 0, buff.length);
                                    ContactItem new_contact = new ContactItem(ip, new String(buff), "");
                                    userList.put(ip, new_contact);
                                }
                                while( iter.hasNext() ) {
                                    Socket socket = iter.next();
                                    //if( !socket.getInetAddress().getHostAddress().equals( ip ) ) {
                                    try {
                                        server.send(socket, buffs );
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //}
                                }
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
