package com.link.platform.media.audio.decode;

import android.util.Log;

import com.link.platform.item.MessageItem;
import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.NativeAudioCodec;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.util.TimeHelper;
import com.link.platform.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioDecoder implements Runnable {

    String TAG = "AudioDecoder";

    private static final int MAX_BUFFER_SIZE = 2048;

    private byte[] decodedData = new byte[1024];// data of decoded
    private boolean isDecoding = false;
    private List<AudioData> dataList = null;
    private String tag;

    private File cache_file;
    private FileOutputStream fos;

    private static Map<String, AudioDecoder> INSTANCES = new HashMap<String, AudioDecoder>();

    public static AudioDecoder getInstance(String tag) {
        if( INSTANCES.containsKey(tag) ) {
            return INSTANCES.get(tag);
        }
        else {
            AudioDecoder decoder = new AudioDecoder(tag);
            decoder.startDecoding();
            return decoder;
        }
    }

    private AudioDecoder(String tag) {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
        INSTANCES.put(tag, this);
        this.tag = tag;
    }

    /*
     * add Data to be decoded
     *
     * @ data:the data recieved from server
     *
     * @ size:data size
     */
    public void addData(byte[] data, int size) {
        Log.d(TAG, "size = " + size );
        try {
            if( size == 3 ) {
                Log.d(TAG,new String(data, "ISO-8859-1") );
            }
            if(size == 3 && ( new String(data, "ISO-8859-1").equals("END") ) ) {
                Log.d(TAG, "VOICE END.....");
                stopDecoding();
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        AudioData adata = new AudioData();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        adata.setData(tempData);
        dataList.add(adata);
        // Log.e(LOG, "添加一次数据 " + dataList.size());

    }

    /*
     * start decode AMR data
     */
    public void startDecoding() {
        Log.d(TAG, "开始解码");
        if (isDecoding) {
            return;
        }
        cache_file = new File(Utils.SD_PATH + Utils.VOICE_CACHE + TimeHelper.currentTime() );
        try {
            fos = new FileOutputStream( cache_file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    public void run() {

        this.isDecoding = true;
        // init ILBC parameter:30 ,20, 15
        NativeAudioCodec.audio_codec_init(30);

        Log.d(TAG,"initialized decoder");
        int decodeSize = 0;
        while (isDecoding) {
            while (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                decodedData = new byte[MAX_BUFFER_SIZE];
                byte[] data = encodedData.getData();
                //
                decodeSize = NativeAudioCodec.audio_decode(data, 0,
                        encodedData.getSize(), decodedData, 0);
                Log.e(TAG, "解码一次 " + data.length + " 解码后的长度 " + decodeSize);
                if (decodeSize > 0) {
                    // add decoded audio to file
                    try {
                        fos.write(decodedData, 0 , decodeSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // clear data
                    decodedData = new byte[decodedData.length];
                }
            }
        }
        Log.d(TAG, "stop decoder");
        INSTANCES.remove(tag);

        MessageWithObject msg = new MessageWithObject();
        msg.setMsgId(MessageTable.MSG_VOICE);
        msg.setObject(MessageItem.voiceMessage(tag, false, cache_file.getPath()));
        MessageCenter.getInstance().sendMessage(msg);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}
