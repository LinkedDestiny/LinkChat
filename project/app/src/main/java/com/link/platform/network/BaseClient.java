package com.link.platform.network;

import com.link.platform.network.socket.IClient;
import com.link.platform.network.socket.MainClient;

/**
 * Created by danyang.ldy on 2014/12/9.
 */
public class BaseClient implements IClient {

    private MainClient client;

    public BaseClient(String host, int port) {
        client = new MainClient(host, port, this);
    }

    @Override
    public void onReceive(String message) {

    }

    @Override
    public void onError(int errno) {

    }


}
