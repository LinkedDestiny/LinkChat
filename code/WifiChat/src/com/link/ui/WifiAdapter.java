package com.link.ui;

import java.util.List;
import java.util.Map;

import com.link.R;
import com.link.tools.Method;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WifiAdapter extends BaseAdapter {
	
	private List<Map<String, Object>> listItems;        
    private LayoutInflater listContainer;
    
    public final class ListItems{
    	public ImageView icon;
    	public TextView SSID;
    	public TextView info;
    }
    
    public WifiAdapter( Context context , List<Map<String , Object>> listItems ){
    	listContainer = LayoutInflater.from(context);   
    	this.listItems = listItems;
    }
    
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
	}
	@Override
	public Object getItem(int arg0) {
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
	          
            convertView = listContainer.inflate(R.layout.wifiadapter, null);     
            listitems.icon = ( ImageView ) convertView.findViewById(R.id.room_icon ); 
            listitems.SSID = (TextView)convertView.findViewById(R.id.wifi_ssid);    
            listitems.info = ( TextView)convertView.findViewById(R.id.wifi_info);    
  
            Method.scaleView(listitems.icon);
            convertView.setTag(listitems);
		}else {    
        	listitems = (ListItems)convertView.getTag();    
        }
		
		listitems.SSID.setText( listItems.get(position).get("SSID").toString().substring(6) );
		listitems.info.setText( (String) listItems.get(position).get("info") );
		Log.v("Adapter" , "update  " + (String) listItems.get(position).get("SSID"));
		return convertView;
	}
}
