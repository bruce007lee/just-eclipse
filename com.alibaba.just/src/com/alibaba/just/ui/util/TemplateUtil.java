package com.alibaba.just.ui.util;

import com.alibaba.just.api.bean.Module;

/**
 * 
 * @author bruce.liz
 *
 */
public class TemplateUtil {
	
	private static TemplateUtil instance = null;
	
	private TemplateUtil(){}
	
	public static TemplateUtil getTemplateUtil(){
		if(instance==null){
			instance = new TemplateUtil();
		}
		return instance;
	}
	
	public String getModulePath(String rootPath,Module module){
		if(module==null){
			return null;
		}
		if(module.isAnonymous()){
			if(rootPath !=null && module.getFilePath()!=null){
				String mp = module.getFilePath().replace('\\', '/');
				int idx = mp.indexOf(rootPath.replace('\\', '/'));
				if(idx>0){
				  return mp.substring(idx + rootPath.length(),mp.length());
				}
			}
			return null;
		}else{
			return module.getName();
		}
	}
}
