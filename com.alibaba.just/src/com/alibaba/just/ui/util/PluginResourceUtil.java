package com.alibaba.just.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.alibaba.just.Activator;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.util.FileUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class PluginResourceUtil {

	private PluginResourceUtil(){}


	/**
	 * 根据相对plugin的路径路径获取文件内容
	 * @param relativeUrl
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String getFile(String relativeUrl,String encode) throws Exception{
		try {
			//new URL("file:tpl/module.tpl")
			if(relativeUrl ==null){
				throw new NullPointerException();
			}
			Bundle bundle = Activator.getDefault().getBundle();
			Path path = new Path(relativeUrl);
			URL fileURL = FileLocator.find(bundle, path, null);
			return FileUtil.getFileContent(fileURL.openStream(), encode);
		}catch (Exception e) {
			throw e;
		}
	}
	
	public static void getModulesByResource(IResource resource,List<Module> list,ModuleParser parser) throws CoreException{
		getModulesByResource(resource,list,parser,ModuleParser.MODULE_TYPE_NORMAL);
	}

	/**
	 * 根据指定的resource获取所有的js模块
	 * @param resource
	 * @param list
	 * @param parser
	 * @throws CoreException
	 */
	public static void getModulesByResource(IResource resource,List<Module> list,ModuleParser parser,int moduleType) throws CoreException{
		if(IContainer.class.isInstance(resource)){
			IContainer pro = (IContainer)resource;
			IResource[] members = pro.members();
			for(int i=0,l=members.length;i<l;i++){
				PluginResourceUtil.getModulesByResource(members[i],list,parser,moduleType);
			}
		}else{
			list.addAll(parser.getAllModules(resource.getLocation().toFile().getAbsolutePath(),moduleType));
		}
	}
	
	
	public static List<Module> getAllModulesByProject(IProject project){
		return getAllModulesByProject(project,ModuleParser.MODULE_TYPE_NORMAL);
	}

	/**
	 * 根据所在project的lib库去获取所有js模块
	 * @param project
	 * @param moduleType
	 * @return
	 */
	public static List<Module> getAllModulesByProject(IProject project,int moduleType){
		List<String> libs = PreferenceUtil.getProjectLibsList(project);	
		List<Module> moduleList = new ArrayList<Module>();
		ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
		parser.setThreadPool(UIUtil.getThreadPool());
		IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();

		for(String lib:libs){
			String lb = lib.trim();
			if(lb.length()>0){

				String folderPath = null;
				String type = PreferenceUtil.getProjectLibType(lb);
				if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||
						PreferenceUtil.LIB_TYPE_SELF.equals(type)){
					if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
						lb = project.getFullPath().toString();
					}else{
						lb = PreferenceUtil.getProjectLibPath(lb);
					}
					IPath rootPath = wRoot.getFullPath();
					IResource res = wRoot.findMember(rootPath.append(lb));
					if(res!=null && res.isAccessible()){
						try{
							PluginResourceUtil.getModulesByResource(res,moduleList,parser,moduleType);
						}catch(Exception e){
							e.printStackTrace();
						}
					}

				}else if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
					lb = PreferenceUtil.getProjectLibPath(lb);
					File f = new File(lb);
					if(f.exists() && f.isDirectory()){
						folderPath = f.getAbsolutePath();
						moduleList.addAll(parser.getAllModules(folderPath));
					}
				}

			}

		}		
		return moduleList;
	}

}
