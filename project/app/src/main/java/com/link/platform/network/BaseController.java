package com.link.platform.network;

import android.util.Log;

import com.link.platform.item.ContactItem;
import com.link.platform.item.NetworkItem;
import com.link.platform.network.socket.IController;
import com.link.platform.network.socket.MainServer;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.Error;

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
    private Thread handlerthread;
    private boolean running = true;
    private List<NetworkItem> dataList;

    public BaseController(int port) {
        server = new MainServer(port, this);
        try {
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataList = Collections.synchronizedList(new LinkedList<NetworkItem>());
        handlerthread = new Thread( new HandleRunnable() );
    }

    public void start() {
        handlerthread.start();
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
        List<Socket> connect_list = server.getConnect_list();
        Iterator<Socket> iter = connect_list.iterator();
        while( iter.hasNext() ) {
            Socket socket = iter.next();
            Log.d(TAG, socket.getInetAddress().getHostAddress() );
        }
    }

    @Override
    public void onReceive(Socket socket, ByteBuffer msg) {
        final String IP = socket.getInetAddress().getHostAddress();
        int type = msg.getInt();
        msg.getInt();
        byte[] buff = new byte[msg.remaining()];
        msg.get(buff);
        Log.d(TAG, "buff size = " + msg.array().length );
        NetworkItem item = new NetworkItem(type, IP, buff, msg.array(), socket);
        synchronized (dataList) {
            dataList.add(item);
        }

    }

    @Override
    public void onClose(Socket fd) {
        Log.e(TAG, fd.getInetAddress().getHostAddress() + " close connection");
        ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_OFFLINE, host_ip , fd.getInetAddress().getHostAddress().getBytes());
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
            fd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class HandleRunnable implements Runnable {

        @Override
        public void run() {
            while( running ) {
                while( dataList.size() > 0 ) {
                    NetworkItem item = dataList.remove(0);
                    Log.d(TAG, "recv a package " + (index ++) + " : size = " + item.getBuff().length);
                    List<Socket> connect_list = server.getConnect_list();
                    Iterator<Socket> iter = connect_list.iterator();
                    int type = item.getMsg_type();
                    if( type == MsgType.MSG_ONLINE ) {
                        index --;
                        // send OnlineList
                        String online_list = ProtocolFactory.parseOnlieList(userList);
                        ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_ONLINE_LIST, host_ip , online_list.getBytes());
                        try {
                            server.send(item.getSocket(), buffer.array());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ContactItem new_contact = new ContactItem(item.getFrom_ip(), new String(item.getBuff()), "");
                        userList.put(item.getFrom_ip(), new_contact);
                    }
                    while( iter.hasNext() ) {
                        Socket socket = iter.next();
                        //if( !socket.getInetAddress().getHostAddress().equals( item.getFrom_ip() ) ) {
                            try {
                                server.send(socket, item.getArray());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        //}
                    }
                }
            }
        }
    }
}
