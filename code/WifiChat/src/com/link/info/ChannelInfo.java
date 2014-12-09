package com.link.info;

import java.nio.channels.SocketChannel;

public class ChannelInfo {
	
	public SocketChannel channel;
	public User user;
	
	public ChannelInfo( SocketChannel channel , User user ){
		this.channel = channel;
		this.user = user;
	}
}
