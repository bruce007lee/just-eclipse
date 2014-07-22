package com.alibaba.just.api.bean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author bruce.liz
 */
public class Module implements Serializable{	
	private static final long serialVersionUID = 7743904505096219050L;
	
	private String name=null;
	private List<String> requiredModuleNames= new ArrayList<String>();
	private String filePath = null;
	private List<String> alias = null;
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
	
	public List<String> getAlias() {
		return alias;
	}
	public void setAlias(List<String> alias) {
		this.alias = alias;
	}
	
	public boolean hasAlias(){
		if(alias!=null && alias.size()>0){
			return true;
		}
		return false;
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
