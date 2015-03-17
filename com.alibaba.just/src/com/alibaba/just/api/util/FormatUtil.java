package com.alibaba.just.api.util;

import java.util.StringTokenizer;

public class FormatUtil {

	private static final char SEP1 = '\\';
	private static final char SEP2 = '/';
	private static final char SEP3 = '.';
	private static final String DOT = ".";
	private static final String SLASH = "/";
	private static final String SEP_REG= "_-.";
	private static final String SEP_CHAR= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DEFAULT_SEP= "-";

	public static String formatPath(String path){
		if(path!=null){
			path = path.replace(SEP1,SEP2);
		}
		return path;
	}

	public static String getPathName(String path){
		if(path!=null){
			String fn = formatPath(path);
			int idx = fn.lastIndexOf(DOT);
			if(idx>0){
				fn = fn.substring(0,idx);
			}
			return fn;
		}
		return path;
	}

	public static String getName(String path){
		if(path!=null){
			String fn = formatPath(path);
			int idx = fn.lastIndexOf(SLASH);
			if(idx>=0){
				fn = fn.substring(idx+1,fn.length());
			}
			idx = fn.lastIndexOf(DOT);
			if(idx>0){
				fn = fn.substring(0,idx);
			}
			return fn;
		}
		return path;
	}

	/**
	 * 只分割 -和_,转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @return
	 */
	public static String toCamel(String str){
		return toCamel(str,false,SEP_REG);
	}

	/**
	 * 只分割 -和_,转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @param isClass
	 * @return
	 */
	public static String toCamel(String str,boolean isClass){
		return toCamel(str,isClass,SEP_REG);
	}

	/**
	 * 转换成驼峰格式
	 * e.g:test-module-a => testModuleA
	 * @param str
	 * @param delim
	 * @return
	 */
	public static String toCamel(String str,boolean isClass,String delim){
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
	public static String toHorLine(String str){
		return toHorLine(str,DEFAULT_SEP);
	}

	private static boolean isSep(String charc ,String sep){
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
	public static String toHorLine(String str,String sep){
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
	public static String toCamelPath(String path,boolean isClass,String delim){
		if(path!=null){
			String newPath = path.replace(SEP1,SEP2).replace(SEP3,SEP2);
			int idx = newPath.lastIndexOf(SLASH);
			if(idx>0){
				path = path.substring(0,idx+1) + toCamel(path.substring(idx+1,path.length()),isClass,delim);
			}else{
				path = toCamel(path,isClass,delim);
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
	public static String toCamelPath(String path,boolean isClass){
		return toCamelPath(path,isClass,SEP_REG);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String toCamelPath(String path){
		return toCamelPath(path,false,SEP_REG);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String toHorLinePath(String path){		
		return toHorLinePath(path,DEFAULT_SEP);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String toHorLinePath(String path,String sep){
		if(path!=null){
			String newPath = path.replace(SEP1,SEP2).replace(SEP3,SEP2);
			int idx = newPath.lastIndexOf(SLASH);
			if(idx>0){
				path = path.substring(0,idx+1) + toHorLine(path.substring(idx+1,path.length()),sep);
			}else{
				path = toHorLine(path,sep);
			}
		}
		return path;
	}

}
