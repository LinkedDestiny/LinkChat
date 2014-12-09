package com.link.platform.network.socket;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public interface IClient {

    public void onReceive(String message);

    public void onError(int errno);
}
