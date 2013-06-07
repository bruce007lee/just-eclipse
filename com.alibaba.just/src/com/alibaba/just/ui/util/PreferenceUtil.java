package com.alibaba.just.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

import com.alibaba.just.Activator;
import com.alibaba.just.PluginConstants;
import com.alibaba.just.ui.preferences.PreferenceConstants;

public class PreferenceUtil {	

	private static final String SEP_REG = "[\n]";
	
	private static final String LIB_SEP= "|";

	public static final String LIB_TYPE_SELF = "s|";
	public static final String LIB_TYPE_EXTERNAL_FOLDER = "e|";
	public static final String LIB_TYPE_WORKSPACE_FOLDER = "w|";
	
	public static final String SELF_LIB_STR = LIB_TYPE_SELF + "<PROJECT>";

	private PreferenceUtil(){}

	/**
	 * 
	 * @param project
	 * @param key
	 * @return
	 */
	public static String getProjectProperty(IProject project,String key){		
		String val = null;		
		try {
			val = project.getPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key));
		} catch (CoreException e) {}		
		return val;
	}


	/**
	 * 
	 * @param project
	 * @param key
	 * @param value
	 * @return
	 */
	public static void setProjectProperty(IProject project,String key,String value){	
		try {
			project.setPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key),value);
		} catch (CoreException e) {
		}
	}



	/**
	 * 获取指定项目的libs path列表
	 * @return
	 */
	public static List<String> getProjectLibsList(IProject project){
		List<String> list = new ArrayList<String>();
		String libsStr = PreferenceUtil.getProjectProperty(project,PluginConstants.LIBS_PROPERTY_KEY);
		if(libsStr!=null){
			String[] libs = libsStr.split(SEP_REG);
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){
					list.add(lb);
				}
			}
		}else{
			list.add(SELF_LIB_STR);	
		}
		return list;
	}

	/**
	 * 根据lib数据获取当前lib的类型
	 * @param libStr
	 * @return
	 */
	public static String getProjectLibType(String libStr){
		if(libStr==null){
			return null;
		}
		if(libStr.startsWith(LIB_TYPE_SELF)){
			return LIB_TYPE_SELF;
		}else if(libStr.startsWith(LIB_TYPE_EXTERNAL_FOLDER)){
			return LIB_TYPE_EXTERNAL_FOLDER;
		}else if(libStr.startsWith(LIB_TYPE_WORKSPACE_FOLDER)){
			return LIB_TYPE_WORKSPACE_FOLDER;
		}else{
			return null;
		}
	}
	
	/**
	 * 根据lib数据获取当前path的类型
	 * @param libStr
	 * @return
	 */
	public static String getProjectLibPath(String libStr){
		if(libStr==null){
			return null;
		}		
		libStr = libStr.trim();
		int idx = libStr.indexOf(LIB_SEP);
		if(idx>0){
			return libStr.substring(idx+LIB_SEP.length(),libStr.length());
		}else{
			return null;
		}
	}

	/**
	 * 获取指定项目的root path列表
	 * @return
	 */
	public static List<String> getProjectRootPathList(IProject project){
		List<String> list = new ArrayList<String>();
		String pathStr = PreferenceUtil.getProjectProperty(project,PluginConstants.ROOT_PATH_PROPERTY_KEY);
		if(pathStr!=null){
			String[] libs = pathStr.split(SEP_REG);
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){
					list.add(lb);
				}
			}
		}
		return list;
	}

	/**
	 * 
	 * @param project
	 * @param ipath
	 */
	public static String getRootPathByIPath(IProject project,String ipath){
		String path = ipath;
		String projectPath = project.getFullPath().toString();
		if(path.indexOf(projectPath)==0){
			if(path.equals(projectPath)){
				path="/";
			}else{
				path = path.substring(projectPath.length());
			}
		}
		return path;
	}

	/**
	 * 
	 * @param project
	 * @param ipath
	 */
	public static String getRootPathByIPath(IProject project,IPath ipath){
		String path = ipath.toString();
		return PreferenceUtil.getRootPathByIPath(project, path);
	}

	/**
	 * 设置指定项目的root path列表
	 */
	public static void setProjectRootPathList(IProject project,List<String> list){
		if(list!=null){
			StringBuffer sb = new StringBuffer();	
			if(list!=null){
				for(int i=0,l=list.size();i<l;i++){
					if(i>0){
						sb.append("\n");
					}
					sb.append(list.get(i));
				}
			}
			PreferenceUtil.setProjectProperty(project,PluginConstants.ROOT_PATH_PROPERTY_KEY, sb.toString());
		}		
	}


	/**
	 * 获取插件的PreferenceStore
	 * @return
	 */
	public static IPreferenceStore getPluginPreferenceStore(){
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * 
	 * @return
	 */
	public static String getFileCharset(){
		String charset = PreferenceUtil.getPluginPreferenceStore().getString(PreferenceConstants.P_FILE_CHARSET);
		if(charset==null || charset.length()<=0){
			charset = PreferenceConstants.DEFAULT_FILE_CHARTSET;
		}
		return charset;
	}
}
