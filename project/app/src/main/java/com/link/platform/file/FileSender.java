package com.link.platform.file;

import com.link.platform.item.MessageItem;
import com.link.platform.network.BaseClient;
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.util.MsgType;
import com.link.platform.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/26.
 */
public class FileSender implements Runnable {

    public final static String TAG = "FileSender";

    private static Map<String, FileSender> INSTANCES;

    public static FileSender getInstance(String tag) {
        if( INSTANCES == null ) {
            INSTANCES = new HashMap<String, FileSender>();
        }
        if( INSTANCES.containsKey(tag) ) {
            return INSTANCES.get(tag);
        } else {
            FileSender sender = new FileSender(tag);
            INSTANCES.put(tag, sender);
            return sender;
        }
    }

    private Thread thread;
    private FileInputStream in;
    private String tag;
    private String filename;
    private int type;

    private FileSender(String tag) {
        this.tag = tag;
        thread = new Thread(this);
    }

    public boolean startThread(String path, int type) {
        this.type = type;
        try {
            in = new FileInputStream(path);
            filename = path.substring( path.lastIndexOf('/') + 1 );
            thread.start();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {

        int n;
        int size = filename.getBytes().length + 5;
        int index = 0;
        byte[] buffer = new byte[Utils.FILE_BUFFER_SIZE];
        buffer = addFilename(buffer, index ++);
        BaseClient client = BaseClient.getInstance(BaseClient.TAG);
        try {
            while( ( n = in.read(buffer , size , Utils.FILE_BUFFER_SIZE - size) ) >= 0 ) {
                if( n > 0 ) {
                    if( type == MsgType.MSG_FILE ) {
                        client.sendMessage(
                                MessageItem.fileMessage(client.getIP(), true,
                                        new String(buffer, 0 , n + size , "ISO-8859-1")) );
                    }
                    else if( type == MsgType.MSG_IMG ) {
                        client.sendMessage(
                                MessageItem.imgMessage(client.getIP(), true,
                                        new String(buffer, 0 , n + size , "ISO-8859-1")));
                    }
                    buffer = addFilename(buffer, index ++);
                }
            }
            buffer[size] = 'E';
            buffer[size + 1] = 'N';
            buffer[size + 2] = 'D';
            client.sendMessage(
                    MessageItem.fileMessage(client.getIP(), true,
                            new String(buffer, 0 , 3 + size , "ISO-8859-1")) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        INSTANCES.remove(tag);
    }

    private byte[] addFilename(byte[] buffer, int index) {
        byte[] name = filename.getBytes();
        buffer[0] = (byte)name.length;
        byte[] index_buff = IOHelper.intToByteArray(index);
        buffer[1] = index_buff[0];
        buffer[2] = index_buff[1];
        buffer[3] = index_buff[2];
        buffer[4] = index_buff[3];
        for(int i = 0 ; i < name.length ; i ++ ) {
            buffer[i + 5] = name[i];
        }
        return buffer;
    }
}
