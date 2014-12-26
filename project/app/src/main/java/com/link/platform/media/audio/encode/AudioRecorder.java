package com.link.platform.media.audio.encode;

import android.media.AudioRecord;
import android.util.Log;

import com.link.platform.media.audio.AudioConfig;
import com.link.platform.media.audio.AudioData;
import com.link.platform.media.audio.AudioInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioRecorder implements Runnable {

    String TAG = "AudioRecorder";

    private boolean isRecording = false;
    private AudioRecord audioRecord;

    private static final int BUFFER_FRAME_SIZE = 480;
    private int audioBufSize = 0;

    //
    private byte[] samples;// data
    // the size of audio read from recorder
    private int bufferRead = 0;
    // samples size
    private int bufferSize = 0;

    private List<AudioData> cache = new ArrayList<AudioData>();

    private boolean isValid = false;
    private boolean isSuccess = false;

    private int mode;

    /*
     * start recording
     */
    public void startRecording(int mode) {
        this.mode = mode;
        bufferSize = BUFFER_FRAME_SIZE;

        audioBufSize = AudioInstance.getInstance().getBuffSize();
        if (audioBufSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "audioBufSize error");
            return;
        }
        samples = new byte[audioBufSize];
        // 初始化recorder
        if (null == audioRecord) {
            audioRecord = AudioInstance.getInstance().getRecorder();
        }
        new Thread(this).start();
    }

    public void isValid() {
        isValid = true;
    }

    /*
     * stop
     */
    public void stopRecording(boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void run() {
        // start encoder before recording
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding( mode );
        Log.d(TAG,"audioRecord startRecording()");
        audioRecord.startRecording();
        Log.d(TAG,"start recording");

        this.isRecording = true;
        while (isRecording) {
            bufferRead = audioRecord.read(samples, 0, bufferSize);
            if (bufferRead > 0) {
                if( isValid ) {
                    // add data to encoder
                    if( cache.size() > 0 ) {
                        encoder.addData(cache);
                        cache.clear();
                    }
                    encoder.addData(samples, bufferRead);
                }
                else {
                    AudioData rawData = new AudioData();
                    rawData.setSize(bufferSize);
                    byte[] tempData = new byte[bufferSize];
                    System.arraycopy(samples, 0, tempData, 0, bufferSize);
                    rawData.setData(tempData);
                    cache.add(rawData);
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cache.clear();
        Log.d(TAG,"end recording");
        audioRecord.stop();
        encoder.stopEncoding(isSuccess);
    }
}
