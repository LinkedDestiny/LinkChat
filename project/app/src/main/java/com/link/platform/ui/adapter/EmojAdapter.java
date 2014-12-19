package com.link.platform.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.link.platform.R;
import com.link.platform.model.EmojModel;

import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/18.
 */
public class EmojAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> list;
    private LayoutInflater inflater;

    public EmojAdapter(Context context, int index) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        list = EmojModel.getInstance().getEmojs(index);
        list.add(R.drawable.del_emoj);
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
        return list.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView emoj;
        if( view == null ) {
            view = View.inflate(context, R.layout.item_emoj, null );
            emoj = (ImageView)view.findViewById(R.id.item_emoj);
            view.setTag(emoj);
        } else {
            emoj = (ImageView)view.getTag();
        }
        emoj.setImageResource( list.get(i) );
        return view;
    }
}
