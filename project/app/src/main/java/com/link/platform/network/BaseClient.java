package com.link.platform.network;

import com.link.platform.item.TextItem;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.network.socket.IClient;
import com.link.platform.network.socket.IOHelper;
import com.link.platform.network.socket.MainClient;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public class BaseClient implements IClient {

    private MainClient client;
    private Map<String, String> online_list;

    public BaseClient(String host, int port) {
        client = new MainClient(host, port, this);

        online_list = new HashMap<String, String>();
    }

    @Override
    public void onReceive(ByteBuffer message) {
        int msg_type = message.getInt();
        String from_ip = IOHelper.ipIntToString(message.getInt());

        byte[] buff = new byte[message.remaining()];
        message.get(buff);
        String content = new String(buff);

        handleMessage(msg_type, from_ip, content);
    }

    @Override
    public void onError(int errno) {

    }

    private void handleMessage(int msg_type, String from_ip, String content) {
        switch ( msg_type ) {
            case MsgType.MSG_ONLINE_LIST: {


            }
            case MsgType.MSG_TEXT: {
                TextItem text = new TextItem();
                text.from_ip = from_ip;
                text.content = content;

                MessageWithObject msg = new MessageWithObject();
                msg.setMsgId(MessageTable.MSG_TEXT);
                msg.setObject( text );
                MessageCenter.getInstance().sendMessage(msg);
            }
            case MsgType.MSG_IMG: {


            }
            case MsgType.MSG_VOICE: {


            }
            case MsgType.MSG_FILE: {


            }
        }
    }

}
