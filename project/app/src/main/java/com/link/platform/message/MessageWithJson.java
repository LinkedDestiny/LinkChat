package com.link.platform.message;

import org.json.JSONObject;


public class MessageWithJson extends BaseMessage {
	private JSONObject m_json_data;
	public JSONObject getJson(){
		return m_json_data;
	}
	public void setJson(JSONObject json){
		m_json_data=json;
	}
}
