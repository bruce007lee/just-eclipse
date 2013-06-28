package com.alibaba.just.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
import com.alibaba.just.api.parser.ParserFactory;
import com.alibaba.just.api.parser.SimpleModuleParser;
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
	 * @return
	 * @throws CoreException
	 */
	public static List<Module> getModulesByResource(IResource resource,List<Module> list,ModuleParser parser,int moduleType) throws CoreException{
		/*if(IContainer.class.isInstance(resource)){
			IContainer pro = (IContainer)resource;
			IResource[] members = pro.members();
			for(int i=0,l=members.length;i<l;i++){
				PluginResourceUtil.getModulesByResource(members[i],list,parser,moduleType);
			}
		}else{
			list.addAll(parser.getAllModules(resource.getLocation().toFile().getAbsolutePath(),moduleType));
		}*/
		if(list==null){list = new ArrayList<Module>();}
		List<String> paths = new ArrayList<String>();
		internalFindModules(resource,list,paths,parser,moduleType);
		list.addAll(parser.getAllModules(paths, moduleType));
		return list;
	}
	
	/**
	 * 根据指定的resource获取所有的js模块路径
	 * @param resource
	 * @param moduleList
	 * @param paths
	 * @param parser
	 * @param moduleType
	 * @return
	 * @throws CoreException
	 */
	private static List<Module> internalFindModules(IResource resource,List<Module> moduleList,List<String> paths,ModuleParser parser,int moduleType) throws CoreException{
		//如果达到限制(10w)条先执行，防止内存消耗过多
		if(paths.size()>100000){
			moduleList.addAll(parser.getAllModules(paths, moduleType));
			paths.clear();
		}
		
		if(parser.getFilter()!=null && !parser.getFilter().accept(resource.getLocation().toFile())){
			return moduleList;
		}
		
		if(IContainer.class.isInstance(resource)){
			IContainer pro = (IContainer)resource;
			IResource[] members = pro.members();
			for(int i=0,l=members.length;i<l;i++){
				PluginResourceUtil.internalFindModules(members[i],moduleList,paths,parser,moduleType);
			}
		}else{
			paths.add(resource.getLocation().toFile().getAbsolutePath());
		}
		return moduleList;
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
		ModuleParser parser = ParserFactory.getModuleParser(PreferenceUtil.getFileCharset());
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
