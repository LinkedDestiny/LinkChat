package com.link.platform.media.audio;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class AudioData {
    int size;
    byte[] realData;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getData() {
        return realData;
    }

    public void setData(byte[] realData) {
        this.realData = new byte[realData.length];
        System.arraycopy(realData,0,this.realData,0,realData.length);
    }

}