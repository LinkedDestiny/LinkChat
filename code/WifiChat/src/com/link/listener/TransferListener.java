package com.link.listener;

import com.link.info.DefaultCharBuffer;

/**
 * listen the action of write and read buffer from socket
 * @author simon
 *
 */
public interface TransferListener {
	
	public void Read( DefaultCharBuffer buff );
	
	public void Write( byte[] buff );
	
}
