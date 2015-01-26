package com.link.platform.network.socket;

import java.nio.ByteBuffer;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public interface IClient {
    /**
     *
     * @param IP
     */
    public void onConnect(String IP);

    /**
     *
     * @param message
     */
    public void onReceive(ByteBuffer message);

    /**
     *
     * @param errno
     */
    public void onError(int errno);
}
