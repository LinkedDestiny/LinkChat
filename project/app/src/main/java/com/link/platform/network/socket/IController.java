package com.link.platform.network.socket;

import java.net.Socket;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public interface IController {

    public void onConnect(Socket fd);

    public void onReceive(Socket fd, String message);

    public void onClose(Socket fd);
}
