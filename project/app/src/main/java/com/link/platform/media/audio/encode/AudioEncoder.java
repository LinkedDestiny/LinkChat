package com.link.platform.media.audio.encode;

import android.util.Log;

import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.NativeAudioCodec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioEncoder implements Runnable {

    String TAG = "AudioEncoder";

    private static AudioEncoder encoder;
    private boolean isEncoding = false;

    private List<AudioData> dataList = null;

    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    public void addData(byte[] data, int size) {
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setData(tempData);
        dataList.add(rawData);
    }

    /*
     * start encoding
     */
    public void startEncoding() {
        Log.d(TAG,"start encode thread");
        if (isEncoding) {
            Log.e(TAG , "encoder has been started  !!!");
            return;
        }
        new Thread(this).start();
    }

    /*
     * end encoding
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    public void run() {
        // start sender before encoder
        AudioSender sender = new AudioSender();
        sender.startSending();

        int encodeSize = 0;
        byte[] encodedData;

        // initialize audio encoder:mode is 30
        NativeAudioCodec.audio_codec_init(30);

        isEncoding = true;
        while (isEncoding) {
            if (dataList.size() == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[rawData.getSize()];
                //
                encodeSize = NativeAudioCodec.audio_encode(rawData.getData(), 0,
                        rawData.getSize(), encodedData, 0);

                if (encodeSize > 0) {
                    sender.addData(encodedData, encodeSize);
                }
            }
        }
        Log.d(TAG,"end encoding");
        sender.stopSending();
    }
}
