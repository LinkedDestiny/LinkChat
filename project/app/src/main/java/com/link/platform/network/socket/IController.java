package com.link.platform.network.socket;

import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public interface IController {

    public void onConnect(Socket fd);

    public void onReceive(Socket fd, ByteBuffer message);

    public void onClose(Socket fd);
}
