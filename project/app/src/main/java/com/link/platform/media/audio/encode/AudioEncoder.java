package com.link.platform.media.audio.encode;

import android.util.Log;

import com.link.platform.item.MessageItem;
import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.AudioManager;
import com.link.platform.media.audio.NativeAudioCodec;
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

    private boolean isSuccess = false;

    private File cache_file;
    private FileOutputStream fos;

    private int mode;

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
        rawData.setData(data);
        dataList.add(rawData);
    }

    public void addData(List<AudioData> list) {
        dataList.addAll(list);
    }

    /*
     * start encoding
     */
    public void startEncoding( int mode ) {
        this.mode = mode;
        Log.d(TAG,"start encode thread");
        if (isEncoding) {
            Log.e(TAG , "encoder has been started  !!!");
            return;
        }
        if( mode == AudioManager.MODE_SHORT_VOICE ) {
            cache_file = new File(Utils.SD_PATH + Utils.VOICE_CACHE + TimeHelper.currentTime() );
            try {
                fos = new FileOutputStream( cache_file );

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        new Thread(this).start();
    }

    /*
     * end encoding
     */
    public void stopEncoding(boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.isEncoding = false;
    }

    public void run() {
        // start sender before encoder
        AudioSender sender = new AudioSender(mode);
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
            if (isEncoding && dataList.size() > 0) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[rawData.getSize()];

                if( mode == AudioManager.MODE_SHORT_VOICE ) {
                    try {
                        fos.write(rawData.getData(), 0 , rawData.getSize());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                encodeSize = NativeAudioCodec.audio_encode(rawData.getData(), 0,
                        rawData.getSize(), encodedData, 0);

                if (encodeSize > 0) {
                    sender.addData(encodedData, encodeSize);
                }
            }
        }
        if( isSuccess && mode == AudioManager.MODE_SHORT_VOICE ) {
            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId(MessageTable.MSG_VOICE);
            msg.setObject(MessageItem.voiceMessage(BaseClient.getInstance(BaseClient.TAG).getIP(), true, cache_file.getPath()));
            MessageCenter.getInstance().sendMessage(msg);
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.setSuccess(isSuccess);
        }
        else {
            Log.d(TAG,"end encoding");
            sender.stopSending(isSuccess);
        }
    }
}
