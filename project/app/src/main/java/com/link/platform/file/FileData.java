package com.link.platform.file;

/**
 * Created by danyang.ldy on 2014/12/19.
 */
public class FileData {
    int size;
    byte[] realData;
    public int p;



    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return realData;
    }

    public void setData(byte[] realData, int offset , int length) {
        this.realData = new byte[realData.length];
        System.arraycopy(realData,offset,this.realData,0,length);
        size = length;
    }

}