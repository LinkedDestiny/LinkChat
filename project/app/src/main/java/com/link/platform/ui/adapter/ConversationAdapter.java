package com.link.platform.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.item.WiFiItem;
import com.link.platform.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class ConversationAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<WiFiItem> list;
    private LayoutInflater inflater;

    private List<WiFiItem> current_list;

    private SimpleFilter mFilter;

    public ConversationAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        list = new ArrayList<WiFiItem>();
        current_list = new ArrayList<WiFiItem>();
    }

    public void setData( List<WiFiItem> list ) {
        if( list == null ) {
            this.current_list = new ArrayList<WiFiItem>();
            return;
        }
        this.current_list = list;
        this.list = new ArrayList<WiFiItem>();
        for( WiFiItem item : list ) {
            this.list.add( new WiFiItem( item.name , item.isLock ));
        }
    }

    private class ViewHolder
    {
        public TextView name;
        public ImageView isLock;
    }

    @Override
    public int getCount() {
        return current_list.size();
    }

    @Override
    public Object getItem(int i) {
        return current_list.get(i);
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
            view = inflater.inflate(R.layout.item_conversation, null);
            viewHolder.name = (TextView)view.findViewById(R.id.item_conversation_name);
            viewHolder.isLock = (ImageView)view.findViewById(R.id.item_conversation_open);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }
        WiFiItem item = current_list.get(i);
        viewHolder.name.setText( item.name.substring(6) );
        if( item.isLock ) {
            viewHolder.isLock.setVisibility(View.VISIBLE);
        } else {
            viewHolder.isLock.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || StringUtil.isBlank(constraint.toString())) {
                //为空时显示所有
                results.count = -1;
                return results;
            }
            String keyStr = constraint.toString().trim().toLowerCase(Locale.getDefault());
            List<WiFiItem> new_list = new ArrayList<WiFiItem>();
            for(WiFiItem item : list) {
                if( item.name.contains( keyStr ) ) {
                    new_list.add(item);
                }
            }
            results.values = new_list;
            results.count = new_list.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            int cnt = results.count;
            List<WiFiItem> new_list = (List<WiFiItem>)results.values;

            if( cnt > 0 ) {
                current_list.clear();
                current_list.addAll(new_list);
                notifyDataSetChanged();
            } else if (cnt == -1) {
                current_list.clear();
                current_list.addAll(list);
                notifyDataSetChanged();
            }
        }
    }
}
