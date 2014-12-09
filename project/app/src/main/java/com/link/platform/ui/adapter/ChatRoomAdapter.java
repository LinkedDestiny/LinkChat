package com.link.platform.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.link.platform.item.WiFiItem;

import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class ChatRoomAdapter extends BaseAdapter {

    private Context context;
    private List<WiFiItem> list;
    private LayoutInflater inflater;

    public ChatRoomAdapter(Context context, List<WiFiItem> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
