package com.alibaba.just.api.bean;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author bruce.liz
 */
public class Module {	
	private String name=null;
	private List<String> requiredModuleNames= new ArrayList<String>();
	private String filePath = null;
	boolean isAnonymous = false;
	
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getRequiredModuleNames() {
		return requiredModuleNames;
	}
	public void setRequiredModuleNames(List<String> requiredModuleNames) {
		this.requiredModuleNames = requiredModuleNames;
	}
	
	/**
	 * test
	 */
	public String toString(){
		String str =  name+"\n" ;
		for(String module:requiredModuleNames){
			str= str+"    "+module+"\n";
		}
		return str;
	}
		
}
