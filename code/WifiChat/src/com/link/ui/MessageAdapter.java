package com.link.ui;

import java.util.List;
import java.util.Map;

import com.link.R;
import com.link.tools.Method;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter{

	private List<Map<String, Object>> listItems;        
    private LayoutInflater listContainer;
    
    public final class ListItems{
    	public ImageView icon;
    	public TextView user;
    	public TextView message;
    }
    
    public MessageAdapter( Context context , List<Map<String , Object>> listItems ){
    	listContainer = LayoutInflater.from(context);
    	this.listItems = listItems;
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItems listitems = null;
		
		if( convertView == null ){
			listitems = new ListItems();     
	          
            convertView = listContainer.inflate(R.layout.messageadapter, null);     
            listitems.icon = ( ImageView ) convertView.findViewById(R.id.userimage ); 
            listitems.user = (TextView)convertView.findViewById(R.id.user_name);    
            listitems.message = ( TextView)convertView.findViewById(R.id.user_message);    
  
            Method.scaleView(listitems.icon);
            convertView.setTag(listitems);
		}else {    
        	listitems = (ListItems)convertView.getTag();    
        }
		
		listitems.user.setText( (String) listItems.get(position).get("Username") );
		listitems.message.setText( (String) listItems.get(position).get("Usermessage") );
		
		return convertView;
	}
}
