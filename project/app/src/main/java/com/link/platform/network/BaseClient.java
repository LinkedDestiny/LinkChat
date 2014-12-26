package com.link.platform.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.link.platform.activity.setting.LocalSetting;
import com.link.platform.item.ContactItem;
import com.link.platform.item.MessageItem;

import com.link.platform.item.NetworkItem;
import com.link.platform.media.audio.AudioManager;
import com.link.platform.media.audio.decode.AudioDecoder;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.model.ContactModel;
import com.link.platform.network.socket.IClient;
import com.link.platform.network.socket.IOHelper;
import com.link.platform.network.socket.MainClient;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.*;
import com.link.platform.util.Error;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Handler handler;
    private HandlerThread handlerthread;

    public BaseClient(String host, int port) {
        client = new MainClient(host, port, this);
        if( INSTANCES.containsKey(TAG) ) {
            INSTANCES.remove(TAG);
        }
        INSTANCES.put( TAG , this );
        handlerthread = new HandlerThread("SendThread");
    }

    public void start() {
        handlerthread.start();
        handler = new Handler(handlerthread.getLooper());
        client.start();
    }

    public void stop() {
        client.stop();
        handlerthread.quit();
        INSTANCES.remove(TAG);
    }

    public String getIP() {
        return IP;
    }

    public boolean sendMessage(MessageItem msg) {
        final MessageItem msgItem = msg;

        handler.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        return true;
    }

    @Override
    public void onConnect(String IP) {
        Log.d(TAG, "Connect with IP: " + IP );
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
        int msg_type = message.getInt();
        String from_ip = IOHelper.ipIntToString(message.getInt());

        byte[] buff = new byte[message.remaining()];
        message.get(buff);
        Log.d(TAG, new String(buff));

        final NetworkItem item = new NetworkItem( msg_type, from_ip, buff);
        handler.post(new Runnable() {
            @Override
            public void run() {
                handleMessage(item.getMsg_type(), item.getFrom_ip(), item.getBuff());
            }
        });

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
        Log.d(TAG, "recv from " + from_ip );
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

                break;
            }
            case MsgType.MSG_VOICE: {
                AudioManager.getInstance().receive(from_ip, buff);
                break;
            }
            case MsgType.MSG_FILE: {

                break;
            }
            case MsgType.MSG_ONLINE: {
                String content = new String(buff);
                ContactModel.getInstance().addContact(new ContactItem(from_ip, content, ""));

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_ONLINE);
                msg.setObject( MessageItem.onlineMessage(from_ip, false, content) );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_OFFLINE: {
                String content = new String(buff);
                Log.d(TAG, content + " off line");
                ContactItem contact =  ContactModel.getInstance().getContact(content);
                if( contact == null )
                    return;
                MessageWithObject msg = new MessageWithObject();
                if(contact.IP.equals("127.0.0.1")) {
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
}
