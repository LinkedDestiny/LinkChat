package com.link.platform.file;

import android.net.Uri;
import android.util.Log;

import com.link.platform.network.socket.IOHelper;
import com.link.platform.util.TimeHelper;
import com.link.platform.util.Utils;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by danyang.ldy on 2014/12/26.
 */
public class FileManager {
    
    private static FileManager Instance;
    public static FileManager getInstance() {
        if( Instance == null ) {
            synchronized( FileManager.class ) {
                if( Instance == null ) {
                    Instance = new FileManager();
                }
            }
        }
        return Instance;
    }
    
    private FileManager() {
        // TODO
    }

    public File createTempFile(String path) {
        File file = new File(Utils.SD_PATH + path + TimeHelper.currentTime() + ".jpg");
        return file;
    }

    public void sendFile(String path, int type) {
        FileSender sender = FileSender.getInstance(path);
        sender.startThread(path, type);
    }

    public void recvFile(String from_ip, byte[] content, String type) {
        FileReceiver receiver;
        int filename_size = content[0];
        int index = IOHelper.bytesToInt(content,1);
        String filename;
        try {
            filename = new String( content , 5 , filename_size , "ISO-8859-1");
            receiver = FileReceiver.getInstance(from_ip + filename);
            receiver.initThread(from_ip, filename , type);

            int content_length = content.length - filename_size - 5;
            if( content_length == 3 && new String(content,filename_size + 5 ,
                    content_length, "ISO-8859-1").equals("END") ) {
                receiver.stopThread();
                return;
            }
            FileData data =  new FileData();
            data.setData(content, filename_size + 5 , content.length - filename_size - 5);
            data.p = index;
            receiver.addData(data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }




}
