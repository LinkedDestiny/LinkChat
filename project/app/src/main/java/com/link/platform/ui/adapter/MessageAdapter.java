package com.link.platform.ui.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.item.ContactItem;
import com.link.platform.item.MessageItem;
import com.link.platform.model.ContactModel;
import com.link.platform.network.util.MsgType;
import com.link.platform.util.ImageLoader;
import com.link.platform.util.SmilyManager;
import com.link.platform.util.StringUtil;

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
        private TextView online;

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
            viewHolder.message = (TextView)view.findViewById(R.id.textView_message);
            viewHolder.online = (TextView)view.findViewById(R.id.online_text);

            viewHolder.picture = (ImageView)view.findViewById(R.id.message_img);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        MessageItem item = list.get(i);
        ContactItem contact = ContactModel.getInstance().getContact(item.from);
        if( contact == null ) {
            contact = new ContactItem();
            contact.IP = item.from;
            contact.name = item.from;
            contact.head = "";
        }
        if( item.msg_type == MsgType.MSG_ONLINE || item.msg_type == MsgType.MSG_OFFLINE ) {
            viewHolder.online.setVisibility(View.VISIBLE);
            view.findViewById(R.id.message_area).setVisibility(View.GONE);

            String content = item.content + (item.msg_type == MsgType.MSG_ONLINE ? "加入" : "离开")
                    + "了群聊";
            viewHolder.online.setText(content);
            return view;
        }
        else {
            view.findViewById(R.id.message_area).setVisibility(View.VISIBLE);
            viewHolder.online.setVisibility(View.GONE);
        }

        if( item.isOwn ) {
            viewHolder.leftHead.setVisibility(View.INVISIBLE);
            viewHolder.rightHead.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().loadImage(viewHolder.rightHead , contact.head , R.drawable.default_avatar );
            viewHolder.sender_name.setVisibility(View.GONE);
            viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            viewHolder.rightHead.setVisibility(View.INVISIBLE);
            viewHolder.leftHead.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().loadImage(viewHolder.leftHead , contact.head , R.drawable.default_avatar );
            viewHolder.sender_name.setVisibility(View.VISIBLE);
            viewHolder.sender_name.setText( contact.name + ":" );
            viewHolder.message.setTextColor(context.getResources().getColor(R.color.content_black));
        }
        switch (item.msg_type) {
            case MsgType.MSG_TEXT:
            {
                handleTextMessage(item, viewHolder);
                break;
            }
            case MsgType.MSG_IMG:
            {
                handleImgMessage(item, viewHolder);
                break;
            }
            case MsgType.MSG_VOICE:
            {
                handleVoiceMessage(item, viewHolder);
                break;
            }
            case MsgType.MSG_FILE:
            {
                handleFileMessage(item, viewHolder);
                break;
            }
        }
        return view;
    }

    private void handleTextMessage(MessageItem item, ViewHolder viewHolder) {
        viewHolder.message.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams textLayout = (RelativeLayout.LayoutParams) viewHolder.message.getLayoutParams();
        if( item.isOwn ) {
            viewHolder.message.setBackgroundResource(R.drawable.chatting_right_content_bg);
            textLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            textLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

        } else {
            viewHolder.message.setBackgroundResource(R.drawable.chatting_left_content_bg);
            textLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            textLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        }
        SpannableStringBuilder buf = new SpannableStringBuilder();
        String msgContent = item.content;
        if (!TextUtils.isEmpty(msgContent)) {
            //换行符替换
            msgContent = replaceNewLineCode(msgContent);
            // 表情
            buf.append(SmilyManager.getInstance().getSmilySpan(msgContent));
        }
        viewHolder.message.setText( buf );

        viewHolder.picture.setVisibility(View.GONE);
    }

    private void handleImgMessage(MessageItem item, ViewHolder viewHolder) {
        viewHolder.picture.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams imgLayout = (RelativeLayout.LayoutParams) viewHolder.picture.getLayoutParams();
        if( item.isOwn ) {
            viewHolder.picture.setBackgroundResource(R.drawable.chatting_right_content_bg);
            imgLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            imgLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

        } else {
            viewHolder.picture.setBackgroundResource(R.drawable.chatting_left_content_bg);
            imgLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            imgLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        }
        ImageLoader.getInstance().loadImage( viewHolder.picture, item.content, R.drawable.default_img );

        viewHolder.message.setVisibility(View.GONE);
    }

    private void handleVoiceMessage(MessageItem item, ViewHolder viewHolder) {

    }

    private void handleFileMessage(MessageItem item, ViewHolder viewHolder) {

    }

    private String replaceNewLineCode(String replaceStr) {
        if (StringUtil.isBlank(replaceStr)) {
            return "";
        }
        replaceStr = replaceStr.replace("\r\n", "\n");
        replaceStr = replaceStr.replace("\r", "\n");
        return replaceStr;
    }
}
