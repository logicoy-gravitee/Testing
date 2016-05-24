package com.luvbrite.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Exceptions {
	
	public Exceptions(){}
	
	public static String giveStackTrace(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);		
		e.printStackTrace(pw);		
		return sw.toString();
	}
}
