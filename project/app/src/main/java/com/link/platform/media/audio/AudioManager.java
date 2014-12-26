package com.link.platform.media.audio;

import com.link.platform.media.audio.decode.AudioDecoder;
import com.link.platform.media.audio.encode.AudioRecorder;

/**
 * Created by danyang.ldy on 2014/12/22.
 */
public class AudioManager {

    public final static int MODE_SHORT_VOICE = 1;
    public final static int MODE_STREAM_VOICE = 2;

    private static AudioManager Instance = null;

    public final static long MIN_TIME = 500;

    public static AudioManager getInstance() {
        if( Instance == null ) {
            synchronized ( AudioManager.class ) {
                if( Instance == null ) {
                    Instance = new AudioManager();
                }
            }
        }
        return Instance;
    }

    private boolean isValid = false;

    private AudioRecorder recorder;

    private AudioManager() {

    }

    public void receive(String tag, byte[] data) {
        AudioDecoder decoder = AudioDecoder.getInstance(tag);
        decoder.addData(data, data.length);
    }

    public void startRecording(int mode) {
        if( recorder == null ) {
            recorder = new AudioRecorder();
            recorder.startRecording(mode);
            isValid = false;
        }
    }

    public void isValid() {
        if( recorder != null ) {
            recorder.isValid();
            isValid = true;
        }
    }


    public boolean stopRecording() {
        if( recorder != null ) {
            recorder.stopRecording(isValid);
            recorder = null;
        }
        return isValid;
    }

    public void cancelRecording() {
        if( recorder != null ) {
            recorder.stopRecording(false);
            recorder = null;
        }
    }

}
