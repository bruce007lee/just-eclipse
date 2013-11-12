/**
 * 
 */
package com.alibaba.just.ui.util;

/**
 * @author bruce.liz
 *
 */
public class LogUtil {

	public static void log(String msg){
		System.out.println(msg);
	}
	
    public static void log(Exception e){
    	System.out.println(e.toString());
	}
	
    public static void log(Class clazz,String msg){
    	System.out.println("["+clazz.getName()+"]"+msg);
	}
    
    public static void log(Class clazz,Exception e){
    	System.out.println("["+clazz.getName()+"]"+e.toString());
	}
	
}
