package com.link.platform.media.audio;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class NativeAudioCodec {

    static {
        System.loadLibrary("NativeAudioCodec");
    }

    // initialize decoder and encoder
    public static native int audio_codec_init(int mode);

    // encode
    public static native int audio_encode(byte[] sample, int sampleOffset,
                                          int sampleLength, byte[] data, int dataOffset);

    // decode
    public static native int audio_decode(byte[] data, int dataOffset,
                                          int dataLength, byte[] sample, int sampleLength);

}