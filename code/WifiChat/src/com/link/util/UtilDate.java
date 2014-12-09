package com.link.util;

import java.util.Calendar;

public class UtilDate {

	public static int getYear(){
		Calendar c = Calendar.getInstance(); 
	    return c.get(Calendar.YEAR);
	}
	
	public static int getMonth(){
		Calendar c = Calendar.getInstance(); 
		return c.get(Calendar.MONTH);
	}
	
	public static int getDay(){
		Calendar c = Calendar.getInstance(); 
		return c.get(Calendar.DAY_OF_MONTH);
	}
	
	public static int getHour(){
		Calendar c = Calendar.getInstance(); 
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinute(){
		Calendar c = Calendar.getInstance(); 
		return c.get(Calendar.MINUTE);
	}
	
	public static int getSeconds() {
		Calendar c = Calendar.getInstance(); 
		return c.get(Calendar.SECOND );
	}
	
	public static String getDate(){
		String result = getYear() + "" + getMonth() + "" + getDay() + "";
		return result;
	}
	
	public static String getTime() {
		String result = getHour() + "" + getMinute() + "" + getSeconds() + "";
		return result;
	}
	
}
