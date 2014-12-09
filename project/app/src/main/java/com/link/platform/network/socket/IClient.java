package com.link.platform.network.socket;

import java.nio.ByteBuffer;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public interface IClient {

    public void onReceive(ByteBuffer message);

    public void onError(int errno);
}
