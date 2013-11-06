package com.alibaba.just.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.alibaba.just.Activator;
import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.api.parser.ParserEvent;
import com.alibaba.just.api.parser.ParserFactory;
import com.alibaba.just.ui.cache.CacheElement;
import com.alibaba.just.ui.cache.ResourceCacheManager;
import com.alibaba.just.util.FileUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class PluginResourceUtil {

	private static final String JAVASCRIPT_EXT = "js";
	private static final String SEARCH_LABEL = "Searching"; //$NON-NLS-1$

	private static PluginResourceUtil instace = new PluginResourceUtil();

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
		if(list==null){list = new ArrayList<Module>();}
		List<String> paths = new ArrayList<String>();
		internalFindModules(resource,list,paths,parser,moduleType);
		list.addAll(parser.getAllModules(paths, moduleType,instace.new ResourceParserEvent()));
		return list;

	}

	public static List<String> getLibPathList(IProject project){
		List<String> libs = PreferenceUtil.getProjectLibsList(project);
		List<String> rs = new ArrayList<String>(5);
		for(String lb:libs){
			String type = PreferenceUtil.getProjectLibType(lb);
			if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
				rs.add(PreferenceUtil.getProjectLibPath(lb));
			}
		}
		return rs;
	}

	public static void clearProjectLibPathCache(IProject project){
		removeProjectLibPathCache(project,null);
	}

	public static void removeProjectLibPathCache(IProject project,List<String> absLibPaths){
		if(project==null){
			return;
		}
		IProject[]  projects = getWorkspace().getRoot().getProjects();
		List<String> libPaths = new ArrayList<String>(5);
		for(int i=0;i<projects.length;i++){
			if(projects[i].isOpen() && !projects[i].equals(project)){
				libPaths.addAll(PluginResourceUtil.getLibPathList(projects[i]));
			}
		}		
		List<String> pLibPaths = (absLibPaths==null?(PluginResourceUtil.getLibPathList(project)) : absLibPaths);
		String tmp = null;
		boolean isExist = false;
		for(int x=0;x<pLibPaths.size();x++){
			tmp = pLibPaths.get(x);
			isExist = false;
			for(int y=0;y<libPaths.size();y++){
				if(tmp.equals(libPaths.get(y))){
					isExist = true;
					break;
				}
			}
			if(!isExist){
				System.out.println("remove lib cache:"+tmp.replace('\\', '/'));
				ResourceCacheManager.remove(tmp.replace('\\', '/'));
			}
		}
	}

	public static List<Module> getModulesByLibPath(IProject project,File f,List<Module> moduleList,ModuleParser parser) throws Exception{

		if(f.exists() && f.isDirectory()){
			String path = f.getAbsolutePath();
			String key = getResourceCacheKey(f).replace('\\', '/');
			CacheElement cache = ResourceCacheManager.get(key);
			if(cache!=null && ((Long)f.lastModified()).equals(cache.getStamp())){
				List<Module> mlist =  (List<Module>) cache.getValue();
				if(mlist!=null){
					moduleList.addAll(mlist);
				}
			}else{
				System.out.println("create lib cache:"+key);
				List<Module> mlist =  parser.getAllModules(path);
				ResourceCacheManager.put(key, new CacheElement(f.lastModified(),mlist));
				moduleList.addAll(mlist);
			}
		}

		return moduleList;
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
			moduleList.addAll(parser.getAllModules(paths, moduleType,instace.new ResourceParserEvent()));
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
			File f = resource.getLocation().toFile();
			String path = f.getAbsolutePath();
			String key = getResourceCacheKey(f);
			CacheElement cache = ResourceCacheManager.get(key);
			if(cache!=null && cache.getStamp()!=null && cache.getStamp().equals(f.lastModified())){
				Object obj = cache.getValue();
				if(List.class.isInstance(obj)){
					List mlist = (List)obj;
					Module mm = null;
					for(Object m:mlist){
						if(Module.class.isInstance(m)){
							mm = (Module)m;
							if(moduleType==parser.MODULE_TYPE_NORMAL && mm.isAnonymous()){							
							}else{
								moduleList.add(mm);
							}
						}
					}
					mm=null;
				}	
				obj= null;
			}else{
				//System.out.println("未命中："+f.getAbsolutePath());
				paths.add(path);
			}
			f = null;
			path=null;
		}
		return moduleList;
	}


	public static List<Module> getAllModulesByProject(ModuleParser parser,IProject project){
		return getAllModulesByProject(parser,project,ModuleParser.MODULE_TYPE_NORMAL);
	}

	public static List<Module> getAllModulesByProject(IProject project){
		return getAllModulesByProject(null,project,ModuleParser.MODULE_TYPE_NORMAL);
	}

	public static List<Module> getAllModulesByProject(IProject project,int moduleType){
		return getAllModulesByProject(null,project,moduleType);
	}

	public static List<Module> getAllModulesByProject(IProject project,int moduleType,IProgressMonitor progressMonitor){
		return getAllModulesByProject(null,project,moduleType,progressMonitor);
	}

	/**
	 * TODO support alias
	 * @return
	 */
	public static List<AliasInfo> getProjectAliasInfo(IProject project){
		List<AliasInfo> alias = new ArrayList<AliasInfo>();

		// UI Components
		/*
		alias.add(new AliasInfo("fui/widget/1.0","lofty/ui/widget/1.0/widget"));
		alias.add(new AliasInfo("fui/tabs/1.0","lofty/ui/tabs/1.0/tabs"));
		alias.add(new AliasInfo("fui/tip/1.0","lofty/ui/tip/1.0/tip"));
		alias.add(new AliasInfo("fui/autocomplete/1.0","lofty/ui/autocomplete/1.0/autocomplete"));
		alias.add(new AliasInfo("fui/suggestion/1.0","lofty/ui/suggestion/1.0/suggestion"));
		alias.add(new AliasInfo("fui/suggestionAll/1.0","lofty/ui/suggestion/1.0/suggestion.all"));			
		alias.add(new AliasInfo("fui/progressbar/1.0","lofty/ui/progressbar/1.0/progressbar"));
		alias.add(new AliasInfo("fui/placeholder/1.0","lofty/ui/placeholder/1.0/placeholder"));
		alias.add(new AliasInfo("fui/paging/1.0","lofty/ui/paging/1.0/paging"));
		alias.add(new AliasInfo("fui/combobox/1.0","lofty/ui/combobox/1.0/combobox"));
		alias.add(new AliasInfo("fui/flash/1.0","lofty/ui/flash/1.0/flash"));
		alias.add(new AliasInfo("fui/flashchart/1.0","lofty/ui/flashchart/1.0/flashchart"));
		alias.add(new AliasInfo("fui/flashuploader/1.0","lofty/ui/flashuploader/1.0/flashuploader"));
		alias.add(new AliasInfo("fui/clipboard/1.0","lofty/ui/flashclipboard/1.0/flashclipboard"));
		alias.add(new AliasInfo("fui/mouse/1.0","lofty/ui/sortable/1.0/mouse"));
		alias.add(new AliasInfo("fui/sortable/1.0","lofty/ui/sortable/1.0/sortable"));
		alias.add(new AliasInfo("fui/dragdrop/1.0","lofty/ui/dragdrop/1.0/dragdrop"));
		alias.add(new AliasInfo("fui/dialog/1.0","lofty/ui/dialog/1.0/dialog"));
		alias.add(new AliasInfo("fui/position/1.0","lofty/ui/position/1.0/position"));
		alias.add(new AliasInfo("fui/timer/1.0","lofty/ui/timer/1.0/timer"));
		*/

		return alias;
	}

	public static List<Module> getAllModulesByProject(ModuleParser parser,IProject project,int moduleType){
		return getAllModulesByProject(parser,project,moduleType,null);
	}

	/**
	 * 根据所在project的lib库去获取所有js模块
	 * @param project
	 * @param moduleType
	 * @return
	 */
	public static List<Module> getAllModulesByProject(ModuleParser parser,IProject project,int moduleType,IProgressMonitor progressMonitor){
		//updataLibPathCacheStatus(project);
		List<String> libs = PreferenceUtil.getProjectLibsList(project);	
		List<Module> moduleList = new ArrayList<Module>();
		boolean isCreate = false;
		if(parser==null){
			isCreate = true;
			parser = ParserFactory.getModuleParser(PreferenceUtil.getFileCharset());
			parser.setAliasList(getProjectAliasInfo(project));//Test only
			parser.setThreadPool(UIUtil.getThreadPool());
		}
		IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
		if(progressMonitor!=null){
			progressMonitor.beginTask(SEARCH_LABEL, libs.size());
		}
		for(String lib:libs){
			if(progressMonitor!=null){
				progressMonitor.worked(1);
			}
			String lb = lib.trim();
			if(lb.length()>0){

				//String folderPath = null;
				String type = PreferenceUtil.getProjectLibType(lb);
				if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||
						PreferenceUtil.LIB_TYPE_SELF.equals(type)){
					if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
						lb = project.getFullPath().toString();
					}else{
						lb = PreferenceUtil.getProjectLibPath(lb);
					}
					if(progressMonitor!=null){
						progressMonitor.setTaskName(SEARCH_LABEL+"["+lb+"]");
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
					if(progressMonitor!=null){
						progressMonitor.setTaskName(SEARCH_LABEL+"["+lb+"]");
					}
					File f = new File(lb);

					try {
						PluginResourceUtil.getModulesByLibPath(project,f,moduleList,parser);
					} catch (Exception e) {}					

					/*if(f.exists() && f.isDirectory()){
						folderPath = f.getAbsolutePath();
						moduleList.addAll(parser.getAllModules(folderPath));
					}*/
				}

			}

		}		

		//updataLibPathCacheStatus(project);

		if(isCreate){
			parser.dispose();
		}

		if(progressMonitor!=null){
			progressMonitor.done();
		}

		return moduleList;
	}

	public static String getResourceCacheKey(IResource resource){
		return getResourceCacheKey(resource.getLocation().toFile());
	}

	public static String getResourceCacheKey(File file){
		return file.getAbsolutePath();
	}

	public static IWorkspace getWorkspace(){
		return ResourcesPlugin.getWorkspace();
	}

	public static String getWorkspacePath(){
		return getWorkspace().getRoot().getLocation().toString();
	}

	/**
	 * 
	 * @author bruce.liz
	 *
	 */
	class ResourceParserEvent implements ParserEvent{
		public void onEnd(ModuleParser parser,File file,List<Module> module) {
			if(file!=null && file.exists() && module!=null){
				ResourceCacheManager.put(getResourceCacheKey(file), new CacheElement(file.lastModified(),module));
			}
		}
		public void onDispose(ModuleParser parser) {}
	}

}
