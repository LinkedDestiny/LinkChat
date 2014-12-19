package com.link.platform.media.audio.decode;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import com.link.platform.media.audio.AudioConfig;
import com.link.platform.media.audio.AudioData;

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
public class AudioPlayer implements Runnable {
    String TAG = "AudioPlayer ";
    private static AudioPlayer player;

    private List<AudioData> dataList = null;
    private AudioData playData;
    private boolean isPlaying = false;

    private AudioTrack audioTrack;

    //
    private File file;
    private FileOutputStream fos;

    private AudioPlayer() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());

        file = new File("/sdcard/audio/decode.amr");
        try {
            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AudioPlayer getInstance() {
        if (player == null) {
            player = new AudioPlayer();
        }
        return player;
    }

    public void addData(byte[] rawData, int size) {
        AudioData decodedData = new AudioData();
        decodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(rawData, 0, tempData, 0, size);
        decodedData.setData(tempData);
        dataList.add(decodedData);
        Log.e(TAG, "Player添加一次数据 " + dataList.size());
    }

    /*
     * init Player parameters
     */
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT);
        if (bufferSize < 0) {
            Log.e(TAG, "initialize error!");
            return false;
        }
        Log.i(TAG, "Player初始化的 buffersize是 " + bufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                AudioConfig.SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
        // set volume:设置播放音量
        audioTrack.setStereoVolume(1.0f, 1.0f);
        audioTrack.play();
        return true;
    }

    private void playFromList() throws IOException {
        while (isPlaying) {
            while (dataList.size() > 0) {
                playData = dataList.remove(0);
                Log.e(TAG, "播放一次数据 " + dataList.size());
                audioTrack.write(playData.getData(), 0, playData.getSize());
                // fos.write(playData.getRealData(), 0, playData.getSize());
                // fos.flush();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void startPlaying() {
        if (isPlaying) {
            Log.e(TAG, "验证播放器是否打开" + isPlaying);
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        this.isPlaying = true;
        if (!initAudioTrack()) {
            Log.i(TAG, "播放器初始化失败");
            return;
        }
        Log.e(TAG, "开始播放");
        try {
            playFromList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
        Log.d(TAG, "end playing");
    }

    public void stopPlaying() {
        this.isPlaying = false;
    }
}
