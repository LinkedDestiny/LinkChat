package com.link.info;

import java.nio.ByteBuffer;

public class DefaultCharBuffer {

	public byte[][] buffer;
	public long messageLength;
	
	public DefaultCharBuffer(){
		
		buffer = new byte[3][];
	}
	
	public void setBuffer( ByteBuffer[] buffer , long count ){
		this.buffer[0] = buffer[0].array();
		this.buffer[1] = buffer[1].array();
		
		this.buffer[2] = new byte[ (int)count - 6 ];
		buffer[2].get(this.buffer[2]);
	}
}
