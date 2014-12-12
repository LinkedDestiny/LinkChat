package com.link.platform.item;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class ContactItem {

    public String IP;
    public String name;
    public String head;

    public ContactItem() {

    }

    public ContactItem(String IP, String name, String head) {
        this.IP = IP;
        this.name = name;
        this.head = head;
    }

    @Override
    public int hashCode() {
        return (IP + name).hashCode();
    }

}
