package com.alibaba.just.ui.util;

import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
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

	/**
	 * 根据指定的resource获取所有的js模块
	 * @param resource
	 * @param list
	 * @param parser
	 * @throws CoreException
	 */
	public static void getModulesByResource(IResource resource,List<Module> list,ModuleParser parser) throws CoreException{
		if(IContainer.class.isInstance(resource)){
			IContainer pro = (IContainer)resource;
			IResource[] members = pro.members();
			for(int i=0,l=members.length;i<l;i++){
				PluginResourceUtil.getModulesByResource(members[i],list,parser);
			}
		}else{
			list.addAll(parser.getAllModules(resource.getLocation().toFile().getAbsolutePath()));
		}
	}
}
