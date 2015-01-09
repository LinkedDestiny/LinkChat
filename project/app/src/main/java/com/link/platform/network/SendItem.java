package com.link.platform.network;

import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by danyang.ldy on 2015/1/4.
 */
public class SendItem {

    public Socket socket;
    public ByteBuffer buffer;

    public SendItem() {

    }

    public SendItem(Socket socket, ByteBuffer buffer) {
        this.socket = socket;
        this.buffer = buffer;
    }
}
