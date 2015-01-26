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
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.util.MsgType;
import com.link.platform.network.util.ProtocolFactory;
import com.link.platform.util.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by danyang.ldy on 2015/1/21.
 */
public class ChatClient extends BaseClient {

    public final static String TAG = "ChatClient";

    public ChatClient(String host, int port) {
        super(host, port);

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
            if( errno != com.link.platform.util.Error.IO_SUCCESS ) {
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
    public void onError(int errno) {

    }

    @Override
    protected void handleMessage(int arg1, int arg2, byte[] content) {
        int msg_type = arg1;
        String from_ip = IOHelper.ipIntToString(arg2);
        if( content.length < 1000 ) {
            Log.d(TAG, new String(content));
        }
        Log.d(TAG, "msg type = " + msg_type);
        switch ( msg_type ) {
            case MsgType.MSG_ONLINE_LIST: {
                Map<String, ContactItem> online_list = ProtocolFactory.getOnlineList(new String(content));
                ContactModel.getInstance().addContacts(online_list, true);

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_ONLINE_LIST);
                msg.setObject( online_list );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_TEXT: {
                String text = new String(content);
                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_TEXT);
                msg.setObject( MessageItem.textMessage(from_ip, false, text) );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_IMG: {
                FileManager.getInstance().recvFile(from_ip, content , MessageTable.MSG_IMG);
                break;
            }
            case MsgType.MSG_VOICE: {
                AudioManager.getInstance().receive(from_ip, content);
                break;
            }
            case MsgType.MSG_FILE: {
                FileManager.getInstance().recvFile(from_ip, content , MessageTable.MSG_FILE);
                break;
            }
            case MsgType.MSG_ONLINE: {
                String name = new String(content);
                Log.d(TAG, name);
                ContactModel.getInstance().addContact(new ContactItem(from_ip, name, ""));

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_ONLINE);
                msg.setObject( MessageItem.onlineMessage(from_ip, false, name) );
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
            case MsgType.MSG_OFFLINE: {
                String name = new String(content);
                ContactItem contact =  ContactModel.getInstance().getContact(name);
                if( contact == null )
                    return;
                MessageWithObject msg = new MessageWithObject();
                if( IOHelper.isHost(contact.IP) ) {
                    msg.setMsgId(MessageTable.MSG_SERVER_CLOSE);
                } else {
                    msg.setMsgId(MessageTable.MSG_OFFLINE);
                }

                msg.setObject( MessageItem.offlineMessage(contact.IP, false, contact.name) );
                ContactModel.getInstance().removeContact(name);
                MessageCenter.getInstance().sendMessage(msg);
                break;
            }
        }
    }

    @Override
    public void onExit() {

    }
}
