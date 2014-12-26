package com.link.platform.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.link.platform.item.ContactItem;
import com.link.platform.network.socket.IController;
import com.link.platform.network.socket.IOHelper;
import com.link.platform.network.socket.MainServer;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.Error;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
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

    private Handler handler;
    private HandlerThread handlerthread;

    public int index = 0;

    public BaseController(int port) {
        server = new MainServer(port, this);
        try {
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handlerthread = new HandlerThread("RecvThread");
    }

    public void start() {
        handlerthread.start();
        handler = new Handler(handlerthread.getLooper());
        server.start();
    }

    public void pause() {
        server.pause();
    }

    public void stop() {
        server.stop();
        handlerthread.quit();
    }


    @Override
    public void onConnect(Socket fd) {
        // TODO boastcast online list

        List<Socket> connect_list = server.getConnect_list();
        Iterator<Socket> iter = connect_list.iterator();
        while( iter.hasNext() ) {
            Socket socket = iter.next();
            Log.d(TAG, socket.getInetAddress().getHostAddress() );
        }
    }

    @Override
    public void onReceive(Socket socket, ByteBuffer msg) {
        final Socket fd = socket;
        final String IP = socket.getInetAddress().getHostAddress();
        final ByteBuffer message = msg;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Send a package " + (index ++) + " : size = " + message.limit());
                List<Socket> connect_list = server.getConnect_list();
                Iterator<Socket> iter = connect_list.iterator();
                Log.d(TAG, "Get message from " + IP);
                int type = message.getInt();
                if( type == MsgType.MSG_ONLINE ) {
                    index --;
                    // send OnlineList
                    String online_list = ProtocolFactory.parseOnlieList(userList);
                    ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_ONLINE_LIST, host_ip , online_list.getBytes());
                    try {
                        int errno = server.send(fd, buffer.array());
                        if( errno != Error.IO_SUCCESS) {
                            Log.e(TAG, "write errno : " + errno );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Add new Contact
                    String IP = IOHelper.ipIntToString( message.getInt() );
                    byte[] buff = new byte[message.remaining()];
                    message.get(buff);
                    String name = new String(buff);
                    ContactItem new_contact = new ContactItem(IP, name, "");
                    userList.put(IP, new_contact);
                }
                while( iter.hasNext() ) {
                    Socket socket = iter.next();
                    if( !socket.getInetAddress().getHostAddress().equals( IP ) ) {
                        try {
                            int errno = server.send(socket, message.array());
                            if( errno != Error.IO_SUCCESS) {
                                Log.e(TAG, "write errno : " + errno );
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onClose(Socket fd) {
        Log.d(TAG, fd.getInetAddress().getHostAddress() + " close connection");
        ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_OFFLINE, host_ip , fd.getInetAddress().getHostAddress().getBytes());
        List<Socket> connect_list = server.getConnect_list();
        Iterator<Socket> iter = connect_list.iterator();
        while( iter.hasNext() ) {
            Socket socket = iter.next();
            if( !socket.getInetAddress().getHostAddress().equals( fd.getInetAddress().getHostAddress() ) ) {
                try {
                    int errno = server.send(socket, buffer.array());
                    if( errno != Error.IO_SUCCESS) {
                        Log.e(TAG, "write errno : " + errno );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
