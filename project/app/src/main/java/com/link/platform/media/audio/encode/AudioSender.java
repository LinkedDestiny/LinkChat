package com.link.platform.media.audio.encode;

import android.util.Log;

import com.link.platform.item.MessageItem;
import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.AudioManager;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.network.BaseClient;
import com.link.platform.util.TimeHelper;
import com.link.platform.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioSender implements Runnable{
    String TAG = "AudioSender";

    private boolean isSendering = false;
    private List<AudioData> dataList;
    private BaseClient client;

    private boolean isSuccess = false;

    public AudioSender(int mode) {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        client = BaseClient.getInstance(BaseClient.TAG);
        if( mode == AudioManager.MODE_STREAM_VOICE ) {
            isSuccess = true;
        }
    }

    public void addData(byte[] data, int size) {
        AudioData encodedData = new AudioData();
        encodedData.setSize(size);
        encodedData.setData(data);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    private void sendData(byte[] data, int size) {
        byte[] buff = new byte[size];
        System.arraycopy(data, 0, buff, 0, size);
        MessageItem item = null;
        try {
            item = MessageItem.voiceMessage(client.getIP(), true, new String(buff, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.sendMessage(item);
    }


    public void setSuccess(boolean isSuccess) {
        AudioData encodedData = new AudioData();
        try {
            encodedData.setData("END".getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        encodedData.setSize(3);
        dataList.add(encodedData);
        this.isSuccess = isSuccess;
    }

    /*
     * start sending data
     */
    public void startSending() {
        new Thread(this).start();
    }

    /*
     * stop sending data
     */
    public void stopSending(boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.isSendering = false;
    }

    // run
    public void run() {
        this.isSendering = true;
        Log.d(TAG, "start....");
        while (isSendering) {
            if (dataList.size() > 0 && isSuccess ) {
                AudioData encodedData = dataList.remove(0);
                if( encodedData.getSize() == 3 ) {
                    Log.d(TAG, new String(encodedData.getData()));
                }
                sendData(encodedData.getData(), encodedData.getSize());
            }
        }

        client = null;
        Log.d(TAG, "stop!!!!");
    }
}
