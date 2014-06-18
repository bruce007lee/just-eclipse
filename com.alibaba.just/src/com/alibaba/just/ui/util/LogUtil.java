/**
 * 
 */
package com.alibaba.just.ui.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.alibaba.just.Activator;

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
    
    public static void error(Exception e){
    	IStatus status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.toString(),e);
		Activator.getDefault().getLog().log(status);
	}
	
}
