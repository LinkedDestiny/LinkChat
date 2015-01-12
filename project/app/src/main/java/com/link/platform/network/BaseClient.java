package com.link.platform.network;

import android.util.Log;

import com.link.platform.activity.setting.LocalSetting;
import com.link.platform.file.FileManager;
import com.link.platform.item.ContactItem;
import com.link.platform.item.MessageItem;

import com.link.platform.media.audio.AudioManager;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.model.ContactModel;
import com.link.platform.network.item.ByteItem;
import com.link.platform.network.socket.IClient;
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.socket.MainClient;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.*;
import com.link.platform.util.Error;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public class BaseClient implements IClient {

    public final static String TAG = "BaseClient";

    private static Map<String, BaseClient> INSTANCES = new HashMap<String, BaseClient>();
    public static BaseClient getInstance(String key) {
        return INSTANCES.get(key);
    }


    private MainClient client;
    private String IP;

    private Thread handleThread;
    private boolean running = true;

    private List<ByteItem> dataList;
    private ByteBuffer buffer;
    private String host;

    public BaseClient(String host, int port) {
        this.host = host;
        client = new MainClient(host, port, this);
        if( INSTANCES.containsKey(TAG) ) {
            INSTANCES.remove(TAG);
        }
        INSTANCES.put( TAG , this );
        dataList = Collections.synchronizedList(new LinkedList<ByteItem>());
        handleThread = new Thread( new HandlerRunnable() );

        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE * 2);
    }

    public void start() {
        handleThread.start();
        client.start();
    }

    public void stop() {
        client.stop();
        running = false;
        INSTANCES.remove(TAG);
    }

    public String getIP() {
        return IP;
    }

    public boolean sendMessage(MessageItem msg) {
        final MessageItem msgItem = msg;
        byte[] bytes;
        try {
            bytes = msgItem.content.getBytes(msgItem.singleByteDecode ? "ISO-8859-1" : "UTF-8");
            ByteBuffer buffer = ProtocolFactory.parseProtocol(msgItem.msg_type, IP, bytes);
            int errno = client.send(buffer.array());
            if( errno != Error.IO_SUCCESS ) {
                Log.e(TAG, "write errno : " + errno );
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onConnect(String IP) {
        Log.d(TAG, "Connect with IP: " + IP );
        if( IOHelper.isHost(host) ) {
            IP = host;
        }
        this.IP = IP;
        ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_ONLINE, IP, LocalSetting.getInstance().getLocalName().getBytes());
        try {
            int errno = client.send(buffer.array());
            if( errno != Error.IO_SUCCESS ) {
                Log.e(TAG, "write errno : " + errno );
            } else {
                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_CONNECT_FINISH);
                msg.setObject( !StringUtil.isBlank(IP) );
                MessageCenter.getInstance().sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReceive(ByteBuffer message) {
        ByteItem item = new ByteItem(message, null);
        Log.d(TAG, "receive: " + item.buffer.length );
        synchronized (dataList) {
            dataList.add(item);
        }
    }

    @Override
    public void onError(int errno) {
        Log.e(TAG, "error: " + errno );
        if( errno == Error.IO_CLOSE || errno == Error.IO_FAILURE ) {
            stop();
            //TODO SEND CLOSE MSG
            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId(MessageTable.MSG_SERVER_CLOSE);
            MessageCenter.getInstance().sendMessage(msg);
        }
    }

    private void handleMessage(int msg_type, String from_ip, byte[] buff) {
        if( buff.length < 1000 ) {
            Log.d(TAG, new String(buff));
        }
        Log.d(TAG, "msg type = " + msg_type);
        switch ( msg_type ) {
            case MsgType.MSG_ONLINE_LIST: {
                String content = new String(buff);
                Map<String, ContactItem> online_list = ProtocolFactory.getOnlineList(content);
                ContactModel.getInstance().addContacts(online_list, true);

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_ONLINE_LIST);
                msg.setObject( online_list );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_TEXT: {
                String content = new String(buff);
                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_TEXT);
                msg.setObject( MessageItem.textMessage(from_ip, false, content) );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_IMG: {
                FileManager.getInstance().recvFile(from_ip, buff , MessageTable.MSG_IMG);
                break;
            }
            case MsgType.MSG_VOICE: {
                AudioManager.getInstance().receive(from_ip, buff);
                break;
            }
            case MsgType.MSG_FILE: {
                FileManager.getInstance().recvFile(from_ip, buff , MessageTable.MSG_FILE);
                break;
            }
            case MsgType.MSG_ONLINE: {
                String content = new String(buff);
                Log.d(TAG, content);
                ContactModel.getInstance().addContact(new ContactItem(from_ip, content, ""));

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_ONLINE);
                msg.setObject( MessageItem.onlineMessage(from_ip, false, content) );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_OFFLINE: {
                String content = new String(buff);
                ContactItem contact =  ContactModel.getInstance().getContact(content);
                if( contact == null )
                    return;
                MessageWithObject msg = new MessageWithObject();
                if( IOHelper.isHost(contact.IP) ) {
                    msg.setMsgId(MessageTable.MSG_SERVER_CLOSE);
                } else {
                    msg.setMsgId(MessageTable.MSG_OFFLINE);
                }

                msg.setObject( MessageItem.offlineMessage(contact.IP, false, contact.name) );
                ContactModel.getInstance().removeContact(content);
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
        }
    }

    class HandlerRunnable implements Runnable {

        @Override
        public void run() {
            while ( running ) {
                synchronized ( dataList ) {
                    while( dataList.size() > 0 ) {
                        ByteItem item = dataList.remove(0);
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
                                System.arraycopy(buffs, 8, buff, 0, buff.length);
                                handleMessage(type, ip, buff);
                            }
                        }
                        buffer.compact();
                    }
                }
            }
        }
    }
}
