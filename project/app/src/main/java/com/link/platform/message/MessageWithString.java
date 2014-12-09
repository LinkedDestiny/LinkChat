package com.link.platform.message;

public class MessageWithString extends BaseMessage {
	private String m_data;
	public String getString(){
		return m_data;
	}
	public void setString(String data){
		m_data=data;
	}
}
