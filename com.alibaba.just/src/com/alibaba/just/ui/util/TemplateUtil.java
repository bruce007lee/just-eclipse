package com.alibaba.just.ui.util;

import java.util.StringTokenizer;

import com.alibaba.just.api.bean.Module;

/**
 * 生成VM模板时,VM内的工具类
 * @author bruce.liz
 *
 */
public class TemplateUtil {
	private static final String SEP_REG= "_-";
	private static final String SEP_CHAR= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DEFAULT_SEP= "-";
	
	private static TemplateUtil instance = null;
	
	private TemplateUtil(){}
	
	public static TemplateUtil getTemplateUtil(){
		if(instance==null){
			instance = new TemplateUtil();
		}
		return instance;
	}
	
	/**
	 * 只分割 -和_,转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @return
	 */
	public String toCamel(String str){
		return toCamel(str,false,SEP_REG);
	}
	
	/**
	 * 只分割 -和_,转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @param isClass
	 * @return
	 */
	public String toCamel(String str,boolean isClass){
		return toCamel(str,isClass,SEP_REG);
	}
	
	/**
	 * 转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @param delim
	 * @return
	 */
	public String toCamel(String str,boolean isClass,String delim){
		if(str!=null){
			StringTokenizer st = new StringTokenizer(str,delim);
			StringBuffer sb = new StringBuffer();
			String seg = null;
			int index = -1;
			while (st.hasMoreTokens()){
				seg = st.nextToken();
				index++;
				if((isClass && index>=0) || (!isClass && index>0)){
					seg = seg.substring(0,1).toUpperCase() + seg.substring(1,seg.length());
				}
				sb.append(seg);
			}
			return sb.toString();
		}
		return str;
	}
	
	/**
	 * 转换成水平线分割格式
	 * e.g:testModuleA => test-module-a
	 * @param str
	 * @return
	 */
	public String toHorLine(String str){
		return toHorLine(str,DEFAULT_SEP);
	}
	
	private boolean isSep(String charc ,String sep){
		if(charc!=null && sep!=null && charc.length()==1 && sep.indexOf(charc)>=0){
			return true;
		}
		return false;
	}
	
	/**
	 * 转换成水平线分割格式
	 * e.g:testModuleA => test-module-a
	 * @param str
	 * @return
	 */
	public String toHorLine(String str,String sep){
		if(str!=null){
			StringTokenizer st = new StringTokenizer(str,SEP_CHAR,true);
			StringBuffer sb = new StringBuffer();
			String seg = null;
			int index = -1;
			while (st.hasMoreTokens()){
				seg = st.nextToken();
				index++;
				
				if(index==0 && seg.length()>0){
					seg = seg.substring(0,1).toLowerCase() + seg.substring(1,seg.length());
				}else if(index>0 && isSep(seg,SEP_CHAR)){
					seg = sep + seg.toLowerCase();
				}
				sb.append(seg);
			}
			return sb.toString();
		}
		return str;
	}
	
	/**
	 * 
	 * @param path
	 * @param isClass
	 * @param delim
	 * @return
	 */
	public String toCamelPath(String path,boolean isClass,String delim){
		if(path!=null){
			String newPath = path.replace('\\', '/').replace('.', '/');
			int idx = newPath.lastIndexOf("/");
			if(idx>0){
				path = path.substring(0,idx+1) + toCamel(path.substring(idx+1,path.length()),isClass,delim);
			}
		}
		return path;
	}

	/**
	 * 
	 * @param path
	 * @param isClass
	 * @return
	 */
	public String toCamelPath(String path,boolean isClass){
		return toCamelPath(path,isClass,SEP_REG);
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String toCamelPath(String path){
		return toCamelPath(path,false,SEP_REG);
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String toHorLinePath(String path){		
		return toHorLinePath(path,DEFAULT_SEP);
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String toHorLinePath(String path,String sep){
		if(path!=null){
			String newPath = path.replace('\\', '/').replace('.', '/');
			int idx = newPath.lastIndexOf("/");
			if(idx>0){
				path = path.substring(0,idx+1) + toHorLine(path.substring(idx+1,path.length()),sep);
			}
		}
		return path;
	}
	
	/**
	 * Get module path in merge file
	 * @param rootPath
	 * @param module
	 * @return
	 */
	public String getModulePath(String rootPath,Module module){
		if(module==null){
			return null;
		}
		if(module.isAnonymous()){
			if(rootPath !=null && module.getFilePath()!=null){
				String mp = module.getFilePath().replace('\\', '/');
				int idx = mp.indexOf(rootPath.replace('\\', '/'));
				if(idx>=0){
				  return mp.substring(idx + rootPath.length(),mp.length());
				}
			}
			return null;
		}else{
			return module.getName();
		}
	}
}
