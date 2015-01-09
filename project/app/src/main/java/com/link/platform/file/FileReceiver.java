package com.link.platform.file;

import android.util.Log;

import com.link.platform.item.MessageItem;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/26.
 */
public class FileReceiver implements Runnable {

    public final static String TAG = "FileReceiver";

    private static Map<String, FileReceiver> INSTANCES;

    public static FileReceiver getInstance(String tag) {
        if( INSTANCES == null ) {
            INSTANCES = new HashMap<String, FileReceiver>();
        }
        if( INSTANCES.containsKey(tag) ) {
            return INSTANCES.get(tag);
        } else {
            FileReceiver sender = new FileReceiver(tag);
            INSTANCES.put(tag, sender);
            return sender;
        }
    }

    private Thread thread;
    private FileOutputStream out;

    private String filename;
    private String from_ip;

    private boolean running = true;
    private boolean isInit = false;
    private String type;
    private int index = 0;

    private List<FileData> dataList;
    private boolean success = true;

    private FileReceiver(String tag) {
        thread = new Thread(this);
        dataList = Collections.synchronizedList(new LinkedList<FileData>());

    }

    public void initThread(String from_ip, String filename, String type) {

        if( !isInit ) {
            this.filename = filename;
            this.from_ip = from_ip;
            this.type = type;
            try {
//                out = new FileOutputStream(Utils.SD_PATH +
//                        ( type.equals(MessageTable.MSG_IMG ) ? Utils.IMG_CACHE : Utils.FILE_CACHE ) + filename );
                out = new FileOutputStream(Utils.SD_PATH + Utils.FILE_CACHE + filename );
                startThread();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            isInit = true;
        }
    }

    public void startThread() {
        thread.start();
    }

    public void stopThread() {
        running = false;
    }

    synchronized public void addData(FileData data) {
        dataList.add(data);
    }

    @Override
    public void run() {

        try {
            while( running ) {
                while( dataList.size() > 0 ) {
                    if( dataList.size() > 0 ) {
                        FileData item = dataList.remove(0);
                        Log.d(TAG, "index = " + item.p);
                        if( index != item.p ) {
                            Log.e(TAG, "wrong package");
                            running = false;
                            success = false;
                            break;
                        }
                        index ++;
                        out.write( item.getData() , 0 , item.getSize() );
                    }
                }
            }
            out.flush();
            out.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        File file = new File(Utils.SD_PATH +
//                ( type.equals(MessageTable.MSG_IMG ) ? Utils.IMG_CACHE : Utils.FILE_CACHE )  + filename );
        File file = new File( Utils.SD_PATH + Utils.FILE_CACHE + filename );
        if( !success ) {

            file.delete();
        } else {
            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId(type);
            if( type.equals(MessageTable.MSG_IMG ) ) {
                msg.setObject(MessageItem.imgMessage(from_ip , false, file.getPath()));
            } else if( type.equals( MessageTable.MSG_FILE )){
                msg.setObject(MessageItem.fileMessage(from_ip , false, file.getPath()));
            }

            MessageCenter.getInstance().sendMessage(msg);
        }
        INSTANCES.remove(this);


    }
}
