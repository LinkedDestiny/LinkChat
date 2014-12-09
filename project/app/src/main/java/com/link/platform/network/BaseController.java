package com.link.platform.network;

import com.link.platform.network.socket.IController;
import com.link.platform.network.socket.MainServer;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class BaseController implements IController {

    private MainServer server;

    public BaseController(int port) {
        server = new MainServer(port, this);
    }

    @Override
    public void onConnect(Socket fd) {
        // TODO boastcast online list
    }

    @Override
    public void onReceive(Socket fd, ByteBuffer message) {
        List<Socket> connect_list = server.getConnect_list();
        Iterator<Socket> iter = connect_list.iterator();

        while( iter.hasNext() ) {
            Socket socket = iter.next();
            if( socket.getInetAddress().getHostAddress().equals( fd.getInetAddress().getHostAddress() ) )
            try {
                server.send(socket, message.array());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClose(Socket fd) {

    }
}
