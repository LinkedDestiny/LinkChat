package com.link.info;

public class User {
	
	public static int USER_TYPE_SERVER = 1;
	public static int USER_TYPE_CLIENT = 2;
	public static int USER_TYPE_UNKNOWN = 3;
	
	private int IP;
	private String Username;
	private int type;
	
	public User( int IP , String Username , int type ){
		this.IP = IP;
		this.Username = new String ( Username );
		this.type = type;
	}
	
	public User( User user ){
		this.IP = user.IP ;
		this.Username = new String ( user.Username );
		this.type = user.type;
	}
	
	public void setUserName( String name ){
		this.Username = name;
	}
	
	public int getIP(){
		return IP;
	}
	
	public String getUserName(){
		return Username;
	}
	
	public int getUserType(){
		return type;
	}
}
