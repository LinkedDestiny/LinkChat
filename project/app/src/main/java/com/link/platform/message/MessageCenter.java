package com.link.platform.message;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MessageCenter extends Handler {
	
	private static MessageCenter m_inst;
	private Map<String, List<MessageListenerDelegate> > m_listener_dic;
	public static MessageCenter getInstance(){
		if(m_inst == null){
			m_inst = new MessageCenter();
		}
		return m_inst;
	}
	
	MessageCenter(){
		super(Looper.getMainLooper());
		this.m_listener_dic = new HashMap<String, List<MessageListenerDelegate>>();
	}
	
	public Map<String, List<MessageListenerDelegate> > getListenerDic(){
		return this.m_listener_dic;
	}
	
	public void setListenerDic(Map<String, List<MessageListenerDelegate> > map){
		this.m_listener_dic=map;
	}
	
	public void registerListener(MessageListenerDelegate listener , String message_id){
		if (listener == null) {
	        Log.e("MessageCenter","Error:Listener is null!");
	        return;
	    }
		List<MessageListenerDelegate> listener_arr = this.m_listener_dic.get(message_id);
	    
	    if (listener_arr == null) {
	        listener_arr = new ArrayList<MessageListenerDelegate>();
	        m_listener_dic.put(message_id, listener_arr);
	    }
	    for (int i = 0; i < listener_arr.size(); i++) {
	        MessageListenerDelegate exist_listener = listener_arr.get(i);
	        //CCAssert(exist_listener != listener, "Error:one message can only be listened by one class once!");
	        if (exist_listener == listener) {
	            return;
	        }
	    }
	    listener_arr.add(listener);
	}
	public void removeListener(MessageListenerDelegate listener){
		Set<String> key_set = ((Set<String>)m_listener_dic.keySet());
		
		Iterator<String> i = key_set.iterator();
        
        while(i.hasNext()){
        	String key = (String)i.next();  
        	List<MessageListenerDelegate> listener_arr = this.m_listener_dic.get(key);
        	if (listener_arr!=null) {
	            listener_arr.remove(listener);
	        }
        } 

	}
	public void removeListenerFromMessage(MessageListenerDelegate listener , String message_id){
		List<MessageListenerDelegate> listener_arr = this.m_listener_dic.get(message_id);
		if (listener_arr!=null) {
            listener_arr.remove(listener);
        }
	}
	@Override
	public void handleMessage(Message msg){
		BaseMessage message = (BaseMessage)msg.obj;
		if (message == null) {
			
	        Log.e("MessageCenter","Error:the Message to send is null!");
	        return ;
	    }
	    
		List<MessageListenerDelegate> listener_arr = this.m_listener_dic.get(message.getMsgId());
	    
	    if (listener_arr == null) {
	    	Log.e("MessageCenter","Error:the message to send didn't exist id="+message.getMsgId());
	        return ;
	    }
	    
	    MessageListenerDelegate listener;
	    for (int i = 0; i < listener_arr.size(); i++) {
	        listener = listener_arr.get(i);
	        listener.getMessage(message);
	    }
	}
	public void sendMessage(BaseMessage message){
		Message msg = new Message();
		msg.what = 1;
		msg.obj = message;
		this.sendMessage(msg);
	}
}
