package com.link.platform.item;

import java.net.Socket;

/**
 * Created by danyang.ldy on 2014/12/24.
 */
public class NetworkItem {

    private Socket socket;

    private int msg_type;
    private String from_ip;
    private byte[] content;

    private byte[] buffer;

    public NetworkItem() {}

    public NetworkItem(int msg_type, String from_ip, byte[] buff) {
        setBuff(buff);
        setFrom_ip(from_ip);
        setMsg_type(msg_type);
    }

    public NetworkItem(int msg_type, String from_ip, byte[] buff, byte[] buffer, Socket socket) {
        setBuff(buff);
        setFrom_ip(from_ip);
        setMsg_type(msg_type);

        this.buffer = new byte[buffer.length];
        System.arraycopy(buffer, 0, this.buffer, 0, buffer.length);
        this.socket = socket;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getFrom_ip() {
        return from_ip;
    }

    public void setFrom_ip(String from_ip) {
        this.from_ip = from_ip;
    }

    public byte[] getBuff() {
        return content;
    }

    public void setBuff(byte[] buff) {
        this.content = new byte[buff.length];
        System.arraycopy(buff,0,this.content,0,buff.length);
    }

    public byte[] getArray() {
        return buffer;
    }

    public Socket getSocket() {
        return socket;
    }
}
