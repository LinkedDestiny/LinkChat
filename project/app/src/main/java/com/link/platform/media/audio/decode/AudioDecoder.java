package com.link.platform.media.audio.decode;

import android.util.Log;

import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.NativeAudioCodec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioDecoder implements Runnable {

    String TAG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private byte[] decodedData = new byte[1024];// data of decoded
    private boolean isDecoding = false;
    private List<AudioData> dataList = null;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
    }

    /*
     * add Data to be decoded
     *
     * @ data:the data recieved from server
     *
     * @ size:data size
     */
    public void addData(byte[] data, int size) {
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
        new Thread(this).start();
    }

    public void run() {
        // start player first
        AudioPlayer player = AudioPlayer.getInstance();
        player.startPlaying();
        //
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
                    // add decoded audio to player
                    player.addData(decodedData, decodeSize);
                    // clear data
                    decodedData = new byte[decodedData.length];
                }
            }
        }
        Log.d(TAG, "stop decoder");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}
