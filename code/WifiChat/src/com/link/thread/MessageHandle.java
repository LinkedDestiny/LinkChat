package com.link.thread;

public class MessageHandle {

	/**
	 * send message to destination
	 * @param Message	User message
	 * @param destination	send to destination, -1 means all users
	 */
	public void sendMessage( String Message , int destination , String type ){
		
	}
	
	/**
	 * get the message recv from other users
	 * @param Message	
	 * @param source	who sends the message
	 */
	public void recvMessage( String Message , int source ){
		
	}
	
	/**
	 * send voice file to destination
	 * @param filename
	 * 		the name of voice file
	 * @param destination
	 * 		send to destination, -1 means all users
	 */
	public void sendVoice( String filename , int destination ){
		
	}
	
	/**
	 *  get voice file from other users
	 * @param filename
	 * 		the voice file name
	 * @param source
	 * 		who sends the message
	 */
	public void recvVoice( String filename , int source ){
		
	}
	
	/**
	 * start network thread
	 */
	public void startThread(){
		
	}
	
	/**
	 * stop newwork thread
	 */
	public void stopThread() {
		
	}
}
