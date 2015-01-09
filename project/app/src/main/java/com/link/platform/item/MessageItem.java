package com.link.platform.item;

import android.util.Log;

import com.link.platform.network.util.MsgType;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class MessageItem {

    public String from;
    public boolean isOwn;
    public String content;
    public int msg_type;
    public boolean singleByteDecode;

    public MessageItem() {

    }

    public MessageItem(String from, boolean isOwn , String content, int msg_type, boolean singleByteDecode) {
        this.from = from;
        this.isOwn = isOwn;
        this.content = content;
        this.msg_type = msg_type;
        this.singleByteDecode = singleByteDecode;
    }

    public static MessageItem textMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_TEXT , false );
    }

    public static MessageItem imgMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_IMG , true );
    }

    public static MessageItem fileMessage(String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_FILE , true );
    }

    public static MessageItem voiceMessage( String from, boolean isOwn, String content ) {
        if( content.length() < 20 ) {
            Log.d("Test", content);
        }
        return new MessageItem( from, isOwn , content , MsgType.MSG_VOICE , true );
    }

    public static MessageItem onlineMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_ONLINE , false );
    }
    public static MessageItem offlineMessage( String from, boolean isOwn, String content ) {
        return new MessageItem( from, isOwn , content , MsgType.MSG_OFFLINE , false );
    }
}
