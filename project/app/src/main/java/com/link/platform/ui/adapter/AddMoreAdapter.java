package com.link.platform.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.item.MenuItem;

/**
 * Created by danyang.ldy on 2014/12/18.
 */
public class AddMoreAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;

    public AddMoreAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    private class ViewHolder
    {
        public ImageView image;
        public TextView name;
    }

    @Override
    public int getCount() {
        return MenuItem.list.size();
    }

    @Override
    public Object getItem(int i) {
        return MenuItem.list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = new ViewHolder();
        if( view == null ) {
            view = View.inflate(context, R.layout.item_add_more, null);
            holder.image = (ImageView)view.findViewById(R.id.item_add_pic);
            holder.name = (TextView)view.findViewById(R.id.item_add_name);

            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        MenuItem item = MenuItem.list.get(i);
        holder.image.setImageResource( item.image );
        holder.name.setText( item.name );
        return view;
    }
}
