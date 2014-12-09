package com.link.info;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;

import com.link.util.Constant;

public class DefaultBuffer {
	
	public ByteBuffer[] buffer;
	
	public DefaultBuffer(){
		buffer = new ByteBuffer[ Constant.BUFF_LENGTH ];
		buffer[0] = ByteBuffer.allocate( Constant.BUFF_IP );
		buffer[1] = ByteBuffer.allocate( Constant.BUFF_TYPE );
		buffer[2] = ByteBuffer.allocate( Constant.BUFF_MESSAGE );
	}
	
	public DefaultBuffer( byte[] bytes ){
		buffer = new ByteBuffer[ Constant.BUFF_LENGTH ];
		buffer[0] = ByteBuffer.wrap(bytes, 0 , Constant.BUFF_IP );
		buffer[1] = ByteBuffer.wrap(bytes, Constant.BUFF_IP , Constant.BUFF_TYPE);
		buffer[2] = ByteBuffer.wrap(bytes, Constant.BUFF_IP + Constant.BUFF_TYPE ,
				bytes.length - Constant.BUFF_IP - Constant.BUFF_TYPE );
	}
	
	public void flip(){
		for( int i = 0 ; i < 3 ; i ++ ){
			buffer[i].flip();
			System.out.println( "Flip  " + buffer[i].array().length ); 
		}
	}
	
	public void clear(){
		for( int i = 0 ; i < 3 ; i ++ )
			buffer[i].clear();
	}
	
	public void compact(){
		for( int i = 0 ; i < 3 ; i ++ )
			buffer[i].compact();
	}
	
	public boolean hasRemaining(){
		for( int i = 0 ; i < 3 ; i ++ ){
			if( buffer[i].hasRemaining() )
				return true;
		}
		return false;
	}
	public CharBuffer[] decode( CharBuffer[] readline ){
		
		try {
			
			for( int i = 0 ; i < 3 ; i ++ )
				readline[i] = Constant.decoder.decode(buffer[i]);
			
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return readline;
	}
}
