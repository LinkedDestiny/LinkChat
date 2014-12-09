package com.link.listener;

import java.nio.channels.SocketChannel;

import com.link.info.DefaultCharBuffer;

public interface ServerTransferListener {

	public void Read( String source , DefaultCharBuffer buff );
	
	public void Write( SocketChannel channel , byte[] buff );
}
