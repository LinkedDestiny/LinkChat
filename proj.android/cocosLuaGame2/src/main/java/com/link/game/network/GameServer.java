package com.link.game.network;

import android.util.Log;

import com.link.platform.item.ContactItem;
import com.link.platform.network.BaseServer;
import com.link.platform.network.item.ByteItem;
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by danyang.ldy on 2015/1/21.
 * an example showing how to use SharingPlatform Network Interface
 */
public class GameServer extends BaseServer {

    public final static String TAG = "GameServer";

    public GameServer(int port) {
        super(port);
        Log.d(TAG, "INIT GameServer");
    }

    @Override
    protected void handleMessage(ByteItem item, int arg1, int arg2, byte[] content) {
        int type = arg1;
        String ip = IOHelper.ipIntToString(arg2);

        List<Socket> connect_list = server.getConnect_list();
        Iterator<Socket> iter = connect_list.iterator();
        Log.d(TAG, new String(content));
        if( type == MsgType.MSG_ONLINE ) {
            // send OnlineList
            String online_list = ProtocolFactory.parseOnlieList(userList);
            ByteBuffer send_msg = ProtocolFactory.parseProtocol(MsgType.MSG_ONLINE_LIST, host_ip , online_list.getBytes());
            try {
                server.send(item.socket, send_msg.array());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContactItem new_contact = new ContactItem(ip, new String(content), "");
            userList.put(ip, new_contact);
        }
        while( iter.hasNext() ) {
            Socket socket = iter.next();
            //if( !socket.getInetAddress().getHostAddress().equals( ip ) ) {
                try {
                    ByteBuffer buffs = ByteBuffer.allocate(8 + content.length);
                    buffs.putInt(arg1);
                    buffs.putInt(arg2);
                    buffs.put(content);
                    server.send(socket, buffs.array() );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
        }
    }

    @Override
    protected void handleConnect(Socket fd) {
        Log.d(TAG, "new client " + fd.getInetAddress().getHostAddress() + " connect");
    }

    @Override
    protected void handleClose(Socket fd) {
        if( fd == null || fd.getInetAddress() == null ) {
            return;
        }
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
}
