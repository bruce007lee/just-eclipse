package com.alibaba.just.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.api.parser.ParserEvent;
import com.alibaba.just.api.parser.ParserFactory;
import com.alibaba.just.api.parser.ParserOptions;
import com.alibaba.just.ui.cache.CacheElement;
import com.alibaba.just.ui.cache.ResourceCacheManager;
import com.alibaba.just.util.FileUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class PluginResourceUtil {

	private static final String SEARCH_LABEL = "Searching"; //$NON-NLS-1$

	private static final String SEP = "@"; //$NON-NLS-1$
	private static final String EMPT = ""; //$NON-NLS-1$

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
	 * 插件中获取ModuleParser的方法,一般不直接使用module api中ParserFactory的方法
	 * @return 根据当前插件的设置生成ModuleParser
	 */
	public static ModuleParser getModuleParser(){
		ParserOptions opts = new ParserOptions();
		opts.setMdType(PreferenceUtil.getMDType());
		opts.setCharset(PreferenceUtil.getFileCharset());
		opts.setDefineKeyWord(PreferenceUtil.getDefineKeyWord());
		return ParserFactory.getModuleParser(opts);
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
		internalFindModules(resource,list,paths,parser,moduleType,true);
		list.addAll(parser.getAllModules(paths, moduleType,getParserEvent(resource)));
		return list;

	}

	public static List<String> getLibList(IProject project){
		List<String> libs = PreferenceUtil.getProjectLibsList(project);
		List<String> rs = new ArrayList<String>(5);
		for(String lb:libs){
			String type = PreferenceUtil.getProjectLibType(lb);
			if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)||PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type)){
				rs.add(lb);
			}
		}
		return rs;
	}

	public static void clearProjectLibCache(IProject project){
		removeProjectLibCache(project,null);
	}

	/**
	 * 
	 * @param project
	 * @param absLibPaths
	 */
	public static void removeProjectLibCache(IProject project,List<String> removeLibs){
		if(project==null){
			return;
		}
		List<String> pLibs = 	PluginResourceUtil.getLibList(project);
		List<String> pRemoveLibs  = (removeLibs==null?pLibs : removeLibs);
		String tmp = null;
		boolean isExist = false;
		for(int x=0;x<pRemoveLibs.size();x++){
			tmp = pRemoveLibs.get(x);
			isExist = false;
			for(int y=0;y<pLibs.size();y++){
				if(tmp.equals(pLibs.get(y))){
					isExist = true;
					break;
				}
			}
			if(isExist){
				System.out.println("remove lib cache:"+getResourceCacheKey(project,tmp));
				ResourceCacheManager.remove(getResourceCacheKey(project,tmp));
			}
		}
	}


	/**
	 * 获取指定project的库文件module列表(包括外部和workspace中的)
	 * @param project
	 * @param resource
	 * @param moduleList
	 * @param parser
	 * @return
	 * @throws Exception
	 */
	public static List<Module> getModulesByLib(IProject project,String libStr,List<Module> moduleList,ModuleParser parser,int moduleType) throws Exception{
		if(moduleList==null){moduleList = new ArrayList<Module>();}
		libStr = libStr.trim();
		String type = PreferenceUtil.getProjectLibType(libStr);
		int mtype = ModuleParser.MODULE_TYPE_NORMAL;
		//根据用户的配置决定是否读取并展示lib库中的匿名模块，默认不读取
		if(PreferenceUtil.isShowLibAnonymouseModule()){
			mtype = ModuleParser.MODULE_TYPE_ALL;
		}
		if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type)){
			/*外部lib*/
			String lb = PreferenceUtil.getProjectLibPath(libStr);
			IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
			IPath rootPath = wRoot.getFullPath();
			IResource res = wRoot.findMember(rootPath.append(lb));

			if(res instanceof IContainer && res.exists()){
				String key = getResourceCacheKey(project,libStr);
				String stamp = res.getPersistentProperty(PluginConstants.CACHE_QUALIFIEDNAME);

				CacheElement cache = ResourceCacheManager.get(key);
				if(stamp!=null && cache!=null && stamp.equals(cache.getStamp())){
					List<Module> mlist =  (List<Module>) cache.getValue();
					if(mlist!=null){
						moduleList.addAll(filterByModuleType(mlist,moduleType,mtype));
					}
				}else{
					List<String> paths = new ArrayList<String>();
					/*lib的话默认都只取非匿名模块*/
					internalFindModules(res,moduleList,paths,parser,mtype,false);
					List<Module> mlist =  parser.getAllModules(paths,mtype);
					if(!parser.isDisposed()){
						//注意下异步时终止时的情况，如果没有做完不应该cache
						stamp = Long.toString(new Date().getTime());
						res.setPersistentProperty(PluginConstants.CACHE_QUALIFIEDNAME, stamp);
						System.out.println("create lib cache:"+key);
						ResourceCacheManager.put(key, new CacheElement(stamp,mlist));
						moduleList.addAll(filterByModuleType(mlist,moduleType,mtype));
					}
				}
			}
		}else if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
			/*workspace中lib*/
			String lb = PreferenceUtil.getProjectLibPath(libStr);
			File f = new File(lb);
			if(f.exists() && f.isDirectory()){
				String path = f.getAbsolutePath();
				String key = getResourceCacheKey(project,libStr);
				CacheElement cache = ResourceCacheManager.get(key);
				if(cache!=null && ((Long)f.lastModified()).equals(cache.getStamp())){
					List<Module> mlist =  (List<Module>) cache.getValue();
					if(mlist!=null){
						moduleList.addAll(filterByModuleType(mlist,moduleType,mtype));
					}
				}else{
					/*lib的话默认都只取非匿名模块*/
					List<Module> mlist =  parser.getAllModules(path,mtype);
					if(!parser.isDisposed()){
						//注意下异步时终止时的情况，如果没有做完不应该cache
						System.out.println("create lib cache:"+key);
						ResourceCacheManager.put(key, new CacheElement(f.lastModified(),mlist));
						moduleList.addAll(filterByModuleType(mlist,moduleType,mtype));
					}
				}
			}
		}
		return moduleList;
	}

	/**
	 * 根据类型过滤缓存的module列表(只当设置读取lib库的匿名模块时才处理，优化效率)
	 * @param moduleList
	 * @param moduleType
	 * @return
	 */
	private static List<Module> filterByModuleType(List<Module> moduleList,int moduleType,int cacheType){
		List<Module> newList = moduleList;
		if(moduleList!=null && ModuleParser.MODULE_TYPE_ALL!= moduleType && cacheType!= moduleType){
			//filter module by type
			newList = new ArrayList<Module>();
			for(Module m:moduleList){
				if(moduleType==ModuleParser.MODULE_TYPE_NORMAL && m.isAnonymous()){							
				}else{
					newList.add(m);
				}
			}
		}
		return newList;
	}

	private static ResourceParserEvent getParserEvent(IResource resource){
		return instace.new ResourceParserEvent(resource.getProject()!=null?resource.getProject().getName():null);
	}

	private static List<Module> internalFindModules(IResource resource,List<Module> moduleList,List<String> paths,ModuleParser parser,int moduleType,boolean useCache) throws CoreException{
		return internalFindModules( resource,moduleList,paths,parser,moduleType,instace.getParserEvent(resource),useCache);
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
	private static List<Module> internalFindModules(IResource resource,List<Module> moduleList,List<String> paths,ModuleParser parser,int moduleType,ResourceParserEvent event,boolean useCache) throws CoreException{
		event = (event == null?instace.getParserEvent(resource):event);
		//如果达到限制(10w)条先执行，防止内存消耗过多
		if(paths.size()>100000){
			moduleList.addAll(parser.getAllModules(paths, moduleType,event));
			paths.clear();
		}

		if(parser.getFilter()!=null && !parser.getFilter().accept(resource.getLocation().toFile())){
			return moduleList;
		}

		if(resource instanceof IContainer){
			IContainer pro = (IContainer)resource;
			IResource[] members = pro.members();
			for(int i=0,l=members.length;i<l;i++){
				PluginResourceUtil.internalFindModules(members[i],moduleList,paths,parser,moduleType,event,useCache);
			}
		}else{
			File f = resource.getLocation().toFile();
			String path = f.getAbsolutePath();

			if(useCache){
				String key = getResourceCacheKey(resource);
				CacheElement cache = ResourceCacheManager.get(key);
				if(cache!=null && cache.getStamp()!=null && cache.getStamp().equals(f.lastModified())){
					Object obj = cache.getValue();
					if(obj instanceof List){
						List mlist = (List)obj;
						Module mm = null;
						for(Object m:mlist){
							if(m instanceof Module){
								mm = (Module)m;
								if(moduleType==ModuleParser.MODULE_TYPE_NORMAL && mm.isAnonymous()){							
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
			}else{
				paths.add(path);
			}
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
	 * Get alias list by project
	 * @return
	 */
	public static List<AliasInfo> getProjectAliasInfo(IProject project){
		return PreferenceUtil.getProjectAliasList(project);
	}

	public static List<Module> getAllModulesByProject(ModuleParser parser,IProject project,int moduleType){
		return getAllModulesByProject(parser,project,moduleType,null);
	}

	public static void clearProjectCache(IProject project){
		List<String> libs = PreferenceUtil.getProjectLibsList(project);
		IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = wRoot.getFullPath();
		IResource res = wRoot.findMember(rootPath);
		if(res!=null && res.isAccessible()){
			try{
				clearResourceCache(res);
			}catch(Exception e){
				e.printStackTrace();
			}
			clearProjectLibCache(project);
		}
	}

	public  static void clearResourceCache(IResource resource){
		ModuleParser p = ParserFactory.getModuleParser();
		clearResourceCache(resource,ParserFactory.getModuleParser());
		p.dispose();
	}

	private static void clearResourceCache(IResource resource,ModuleParser parser){
		if(parser.getFilter()!=null && !parser.getFilter().accept(resource.getLocation().toFile())){

		}else{
			if(resource instanceof IContainer){
				IContainer pro = (IContainer)resource;
				IResource[] members=null;
				try {
					members = pro.members();
					for(int i=0,l=members.length;i<l;i++){
						clearResourceCache(members[i],parser);
					}
				} catch (CoreException e) {
				}
			}else{
				String key = getResourceCacheKey(resource);
				ResourceCacheManager.remove(key);
			}
		}
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
			parser = PluginResourceUtil.getModuleParser();
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

				String type = PreferenceUtil.getProjectLibType(lb);
				if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
					lb = project.getFullPath().toString();
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

				}else if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
					try {
						PluginResourceUtil.getModulesByLib(project,lb,moduleList,parser,moduleType);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		}		

		//updataLibPathCacheStatus(project);

		if(isCreate){
			parser.dispose();
		}

		if(progressMonitor!=null){
			//progressMonitor.done();
		}

		return moduleList;
	}

	public static String getResourceCacheKey(IResource resource){
		if(resource.getProject()!=null){
			return getResourceCacheKey(resource.getProject().getName(),resource.getLocation().toFile());
		}
		return getResourceCacheKey(EMPT,resource.getLocation().toFile());
	}

	public static String getResourceCacheKey(IProject project  ,File file){
		if(project!=null){
			return getResourceCacheKey(project.getName() ,file);
		}
		return getResourceCacheKey(EMPT,file);
	}

	/**
	 * 注意此key只用于project lib的key
	 * @param project
	 * @param str
	 * @return
	 */
	public static String getResourceCacheKey(IProject project ,String str){
		String prefix = EMPT;
		if(project!=null){
			prefix =  project.getName();
		}
		return (prefix==null?EMPT:prefix)+SEP+str;
	}


	public static String getResourceCacheKey(String prefix ,File file){
		return (prefix==null?EMPT:prefix)+SEP+file.getAbsolutePath();
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
		private String prefix = null;
		ResourceParserEvent(String prefix){
			this.prefix = prefix;
		}
		public void onParseFileSuccess(ModuleParser parser,File file,List<Module> module) {
			if(file!=null && file.exists() && module!=null){
				ResourceCacheManager.put(getResourceCacheKey(prefix,file), new CacheElement(file.lastModified(),module));
			}
		}
		public void onParseFileEnd(ModuleParser parser,File file) {}
	}

}
