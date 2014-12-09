package com.link.platform.message;


public class MessageWithObject extends BaseMessage {
	private Object m_data;
	public Object getObject(){
		return m_data;
	}
	public void setObject(Object obj){
		m_data=obj;
	}
}
