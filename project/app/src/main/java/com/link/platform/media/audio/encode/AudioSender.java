package com.link.platform.media.audio.encode;

import android.util.Log;

import com.link.platform.item.MessageItem;
import com.link.platform.media.audio.AudioData;
import com.link.platform.network.BaseClient;

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

    public AudioSender() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        client = BaseClient.getInstance(BaseClient.TAG);
    }

    public void addData(byte[] data, int size) {
        AudioData encodedData = new AudioData();
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        encodedData.setData(tempData);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    private void sendData(byte[] data, int size) {
        // TODO
//        ByteBuffer buffer = ProtocolFactory.parseProtocol(MsgType.MSG_VOICE, client.getIP(), data, size);
//        client.s
        byte[] buff = new byte[size];
        System.arraycopy(buff,0,data,0,size);
        try {
            MessageItem item = MessageItem.voiceMessage(client.getIP(), true, new String(buff, "ISO-8859-1") );
            client.sendMessage(item);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
    public void stopSending() {
        this.isSendering = false;
    }

    // run
    public void run() {
        this.isSendering = true;
        Log.d(TAG, "start....");
        while (isSendering) {
            if (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                sendData(encodedData.getData(), encodedData.getSize());
            }
        }
        Log.d(TAG, "stop!!!!");
    }
}
