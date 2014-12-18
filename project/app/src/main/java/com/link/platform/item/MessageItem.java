package com.link.platform.item;

import com.link.platform.network.MsgType;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class MessageItem {

    public String from;
    public boolean isOwn;
    public String content;
    public int msg_type;

    public MessageItem() {

    }

    public MessageItem(String from, boolean isOwn , String content, int msg_type) {
        this.from = from;
        this.isOwn = isOwn;
        this.content = content;
        this.msg_type = msg_type;
    }

    public static MessageItem textMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_TEXT );
    }

    public static MessageItem imgMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_IMG );
    }

    public static MessageItem voiceMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_VOICE );
    }

    public static MessageItem onlineMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_ONLINE );
    }
    public static MessageItem offlineMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_OFFLINE );
    }
}
