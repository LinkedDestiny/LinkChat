package com.link.platform.ui.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.item.ContactItem;
import com.link.platform.item.MessageItem;
import com.link.platform.model.ContactModel;
import com.link.platform.network.MsgType;

import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<MessageItem> list;
    private LayoutInflater inflater;

    private class ViewHolder
    {
        private ImageView leftHead;
        private ImageView rightHead;

        private TextView sender_name;
        private TextView message;

        private ImageView picture;

        // TODO Voice and file

    }

    public MessageAdapter(Context context, List<MessageItem> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null ) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.item_chat, null);

            viewHolder.leftHead = (ImageView)view.findViewById(R.id.left_head);
            viewHolder.rightHead = (ImageView)view.findViewById(R.id.right_head);

            viewHolder.sender_name = (TextView)view.findViewById(R.id.message_sender_name);
            viewHolder.sender_name = (TextView)view.findViewById(R.id.message_sender_name);

            viewHolder.picture = (ImageView)view.findViewById(R.id.message_img);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        MessageItem item = list.get(i);
        ContactItem contact = ContactModel.getInstance().getContact(item.from);
        if( item.isOwn ) {
            viewHolder.leftHead.setVisibility(View.INVISIBLE);
            viewHolder.rightHead.setVisibility(View.VISIBLE);

        } else {

        }
        switch (item.msg_type) {
            case MsgType.MSG_TEXT:
            {
                handleTextMessage(item, viewHolder);
            }
            case MsgType.MSG_IMG:
            {
                handleImgMessage(item, viewHolder);
            }
            case MsgType.MSG_VOICE:
            {
                handleVoiceMessage(item, viewHolder);
            }
            case MsgType.MSG_FILE:
            {
                handleFileMessage(item, viewHolder);
            }
        }
        return view;
    }

    private void handleTextMessage(MessageItem item, ViewHolder viewHolder) {

    }

    private void handleImgMessage(MessageItem item, ViewHolder viewHolder) {

    }

    private void handleVoiceMessage(MessageItem item, ViewHolder viewHolder) {

    }

    private void handleFileMessage(MessageItem item, ViewHolder viewHolder) {

    }
}
