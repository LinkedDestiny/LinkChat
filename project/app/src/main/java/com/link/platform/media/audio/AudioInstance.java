package com.link.platform.media.audio;

import android.media.AudioRecord;
import android.util.Log;

/**
 * Created by danyang.ldy on 2014/12/24.
 */
public class AudioInstance {
    
    private static AudioInstance Instance;
    public static AudioInstance getInstance() {
        if( Instance == null ) {
            synchronized( AudioInstance.class ) {
                if( Instance == null ) {
                    Instance = new AudioInstance();
                }
            }
        }
        return Instance;
    }

    private AudioRecord recorder;
    private int buffSize;

    private AudioInstance() {
        // TODO
        buffSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.RECORDER_CHANNEL_CONFIG, AudioConfig.AUDIO_FORMAT);
        if (buffSize == AudioRecord.ERROR_BAD_VALUE) {
            return;
        }

        // 初始化recorder
        if (null == recorder) {
            recorder = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.RECORDER_CHANNEL_CONFIG,
                    AudioConfig.AUDIO_FORMAT, buffSize);
        }
    }

    public int getBuffSize() {
        return buffSize;

    }

    public AudioRecord getRecorder() {
        return recorder;
    }
}
