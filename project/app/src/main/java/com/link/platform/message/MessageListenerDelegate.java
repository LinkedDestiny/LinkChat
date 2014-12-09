package com.link.platform.message;

public interface MessageListenerDelegate {
	public void onListenerExit();
	public void getMessage(BaseMessage message);
}
