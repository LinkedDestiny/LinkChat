package com.link.platform.network.item;

import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by danyang.ldy on 2015/1/9.
 */
public class ByteItem {

    public Socket socket;
    public byte[] buffer;

    public ByteItem(ByteBuffer buffer, Socket socket) {
        byte[] byte_array = buffer.array();
        this.buffer = new byte[buffer.remaining()];
        System.arraycopy(byte_array,0 , this.buffer, 0 , buffer.remaining());
        this.socket = socket;
    }
}
