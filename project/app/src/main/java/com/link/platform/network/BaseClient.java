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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
abstract public class BaseClient implements IClient {

    public final static String TAG = "BaseClient";

    protected static Map<String, BaseClient> INSTANCES = new HashMap<String, BaseClient>();
    public static BaseClient getInstance(String key) {
        return INSTANCES.get(key);
    }

    public static void release() {
        Iterator<BaseClient> iter = INSTANCES.values().iterator();
        while (iter.hasNext()) {
            BaseClient client = iter.next();
            client.onExit();
            iter.remove();
        }
    }

    protected MainClient client;
    protected String IP;
    protected String key;

    protected Thread handleThread;
    protected boolean running = true;

    protected List<ByteItem> dataList;
    protected ByteBuffer buffer;
    protected String host;

    public BaseClient(String host, int port) {
        this.host = host;
        client = new MainClient(host, port, this);

        dataList = Collections.synchronizedList(new LinkedList<ByteItem>());
        handleThread = new Thread( new HandlerRunnable() );

        buffer = ByteBuffer.allocate(Utils.BUFFER_SIZE * 2);

        key = host + port;
        if( INSTANCES.containsKey(key) ) {
            INSTANCES.remove(key);
        }
        INSTANCES.put( key , this );
    }

    public void start() {
        handleThread.start();
        client.start();
    }

    public void stop() {
        client.stop();
        running = false;
        INSTANCES.remove(key);
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
    public void onReceive(ByteBuffer message) {
        ByteItem item = new ByteItem(message, null);
        Log.d(TAG, "receive: " + item.buffer.length );
        synchronized (dataList) {
            dataList.add(item);
        }
    }

    abstract protected void handleMessage(int arg1, int arg2, byte[] content);
    abstract public void onExit();

    protected class HandlerRunnable implements Runnable {
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
                                int arg1 = IOHelper.bytesToInt(buffs, 0);
                                int arg2 = IOHelper.bytesToInt(buffs, 4);
                                byte[] content = new byte[ len - 8 ];
                                System.arraycopy(buffs, 8, content, 0, content.length);

                                handleMessage(arg1, arg2, content);
                            }
                        }
                        buffer.compact();
                    }
                }
            }
        }
    }
}
