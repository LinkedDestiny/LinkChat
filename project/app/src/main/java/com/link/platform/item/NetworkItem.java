package com.link.platform.item;

import android.util.Log;

/**
 * Created by danyang.ldy on 2014/12/24.
 */
public class NetworkItem {
    private int msg_type;
    private String from_ip;
    private byte[] buff;

    public NetworkItem() {}

    public NetworkItem(int msg_type, String from_ip, byte[] buff) {
        setBuff(buff);
        setFrom_ip(from_ip);
        setMsg_type(msg_type);
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
        return buff;
    }

    public void setBuff(byte[] buff) {
        this.buff = new byte[buff.length];
        System.arraycopy(buff,0,this.buff,0,buff.length);
    }
}
