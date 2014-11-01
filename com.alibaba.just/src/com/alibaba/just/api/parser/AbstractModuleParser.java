package com.alibaba.just.api.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.converter.NameConverter;
import com.alibaba.just.api.converter.impl.NodeJsNameConverter;
import com.alibaba.just.api.exception.ModuleParseException;

public abstract class AbstractModuleParser implements ModuleParser{
	protected static final String DEFINE_KEY_REG = "^(\\w+\\.)*(define)$";
	protected static final String REQUIRE_KEY_REG = "^require$";
	protected static final NodeJsNameConverter DEFAULT_CMD_CONVERT = new NodeJsNameConverter();
	protected static final int TASK_LIMT = 100000;//用线程池时限制10w个task一次

	protected ExecutorService threadPool = null;
	protected String charset = DEFAULT_CHARSET;
	protected boolean isDispose = false;
	protected FileFilter filter = DEFAULT_FILE_FILTER; 
	protected List<AliasInfo> aliasList = null;		
	protected int mdType = MD_TYPE_UMD;
	protected NameConverter nameConverter = null;
	protected boolean isNodeJs = false;
	protected String defineKeyWord=DEFINE_KEY_REG;
	protected String requireKeyWord=REQUIRE_KEY_REG;
	protected boolean removeDuplicateRequire=true;

	public AbstractModuleParser(ParserOptions options){

		if(options!=null){
			if(options.getCharset()!=null){
				this.setCharset(options.getCharset());
			}
			String defineKeyWord = DEFINE_KEY_REG;
			if(options.getDefineKeyWord()!=null){
				defineKeyWord = options.getDefineKeyWord();
			}
			this.setDefineKeyWord(defineKeyWord);
			String requireKeyWord = REQUIRE_KEY_REG;
			if(options.getRequireKeyWord()!=null){
				requireKeyWord = options.getRequireKeyWord();
			}
			this.setRequireKeyWord(requireKeyWord);
			this.setMdType(options.getMdType());
			this.setIsNodeJs(options.isNodeJs());
			this.setAliasList(options.getAliasList());
			this.setIsRemoveDuplicateRequire(options.isRemoveDuplicateRequire());
			this.setNameConverter(options.getNameConverter());
		}

	}

	public AbstractModuleParser(){}

	/**
	 * 处理folder中文件
	 * @param file
	 * @param list
	 * @param moduleType
	 * @return
	 *//*
	private List<Module> processFolder(File file,final List<Module> list,final int moduleType){
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		this.processFolder(file, list, moduleType,tasks);
		if(threadPool!=null){
			try {
				threadPool.invokeAll(tasks);
				tasks.clear();
				tasks=null;
			} catch (InterruptedException e) {}
		}
		return list;
	}*/

	/**
	 * 并不一定执行，当使用线程池时只处理task队列
	 */
	protected void processFolder(File file,final List<Module> list,final int moduleType,List<Callable<Object>> tasks,final ParserEvent event){
		if(isDispose && !file.exists() || !file.isDirectory()){return;}
		File[]  flist  = file.listFiles(filter);
		for(final File f:flist){
			if(f.isFile()){
				if(threadPool!=null){
					/*当使用线程池时*/
					tasks.add(new Callable<Object>(){
						public Object call() throws Exception {
							list.addAll(getModules(f,moduleType,event));
							return null;
						}					
					});
					//超过限制先执行释放资源
					if(tasks.size()>TASK_LIMT){
						try {
							threadPool.invokeAll(tasks);
							tasks.clear();
						} catch (InterruptedException e) {}
					}
				}else{
					/*当不使用线程池时*/
					list.addAll(getModules(f,moduleType,event));
				}
			}else if(f.isDirectory()){
				processFolder(f,list,moduleType,tasks,event);
			}
		}
	}

	/**
	 * 模块是否在列表中重复
	 * @param module
	 * @param list
	 * @return
	 */
	protected boolean isDuplicate(Module module,List<Module> list){	
		if(module.getName()==null){
			return false;
		}
		for(Module mod : list){
			if(mod.getName()!=null && mod.getName().equals(module.getName())){
				return true;
			}
		}		
		return false;
	}


	/**
	 * 递归得到指定模块的所有依赖子模块
	 * (如果发生循环依赖的话抛出异常)
	 * @param module
	 * @param moduleTree
	 * @param newList
	 * @param list
	 * @return
	 * @throws ModuleParseException 
	 */
	protected List<Module> getAllRequiredModules(Module module,List<Module> moduleTree,List<Module> newList,List<Module> list) throws ModuleParseException{
		List<String> subModNames = module.getRequiredModuleNames();

		Module subMod = null;
		for(String subname : subModNames){
			subMod = getModuleByName(subname, list);

			if(subMod==null && subname!=null){
				subMod = new Module();
				subMod.setName(subname);
			}

			if(subMod!=null){
				if(!isDuplicate(subMod, newList)){
					newList.add(subMod);
				}

				//检测是否有循环引用的情况
				if(isDuplicate(module, moduleTree)){
					throw new ModuleParseException("Import loop with module:[name:"+module.getName()+" file:"+module.getFilePath()+"]");
				}

				List<Module> tree =  new ArrayList<Module>();
				tree.addAll(moduleTree);
				tree.add(module);

				getAllRequiredModules(subMod, tree , newList, list);
				tree.clear();
			}
		}

		return newList;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getAllRequiredModules(com.alibaba.just.api.bean.Module, java.util.List)
	 */
	public List<Module> getAllRequiredModules(Module module,List<Module> list) throws ModuleParseException{
		return getAllRequiredModules(module,new ArrayList<Module>(), new ArrayList<Module>() ,list);
	}


	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getUsedModules(com.alibaba.just.api.bean.Module, java.util.List)
	 */
	public List<Module> getUsedModules(Module module,List<Module> list){
		List<String> subModNames = null;
		List<Module> rsList = new ArrayList<Module>();
		for(Module m : list){
			subModNames = m.getRequiredModuleNames();
			for(String subName : subModNames){
				if((module.getName() != null && module.getName().equals(subName)) || 
						ParserUtil.isMatchAlias(subName, module)){
					rsList.add(m);
					break;
				}
			}
		}
		return rsList;
	}

	/**
	 * 
	 * @param moduleName NOTE: it maybe a module alias name
	 * @param list
	 * @return
	 */
	protected Module getModuleByName(String moduleName,List<Module> list){
		if(moduleName!=null && list!=null){
			for(Module submod : list){
				if(moduleName.equals(submod.getName()) || ParserUtil.isMatchAlias(moduleName, submod)){
					return submod;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getAllModules(java.lang.String)
	 */
	public List<Module> getAllModules(String path){
		return getAllModules(path,MODULE_TYPE_NORMAL);
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getAllModules(java.lang.String, int)
	 */
	public List<Module> getAllModules(String path,int moduleType){
		List<String> paths = new ArrayList<String>(1);
		paths.add(path);
		return this.getAllModules(paths, moduleType);
	}

	/**
	 * 
	 */
	public List<Module> getAllModules(List<String> paths,final int moduleType,final ParserEvent event){
		final List<Module> moduleList = new Vector<Module>();
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		File  file = null;
		for(final String path : paths){
			file = new File(path);
			if(!isDispose){
				if(file.exists()){
					if(file.isDirectory()){
						processFolder(file, moduleList,moduleType,tasks,event);						
					}else if(file.isFile()){
						if(threadPool!=null){
							tasks.add(new Callable<Object>(){
								public Object call() throws Exception {
									moduleList.addAll(getModules(path,moduleType,event));
									return null;
								}					
							});
							//超过限制先执行释放资源
							if(tasks.size()>TASK_LIMT){
								try {
									threadPool.invokeAll(tasks);
									tasks.clear();
								} catch (InterruptedException e) {}
							}

						}else{
							moduleList.addAll(getModules(file,moduleType,event));
						}
					}
				}
			}
		}
		if(threadPool!=null){
			try {
				threadPool.invokeAll(tasks);
				tasks.clear();
				tasks=null;
			} catch (InterruptedException e) {}
		}
		return moduleList;

	}


	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getAllModules(java.util.List, int)
	 */
	public List<Module> getAllModules(List<String> paths,final int moduleType){
		return getAllModules(paths,moduleType,null);
	}

	/**
	 * Update module alias with alias list
	 * @param module
	 * @param aliasList
	 * @return
	 */
	protected Module updateAlias(Module module,List<AliasInfo> aliasList){
		if(module!=null && aliasList!=null){
			List<String> alias = new ArrayList<String>(1);
			for(AliasInfo ai:aliasList){
				if(ai.getName()!=null && ai.getAlias()!=null && module.getName()!=null){
					if(ai.getName().equals(module.getName()) && !alias.contains(ai.getAlias())){
						alias.add(ai.getAlias());
					}
				}
			}
			module.setAlias(alias);
		}
		return module;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.lang.String, int)
	 */
	public List<Module> getModules(String filePath,int moduleType,ParserEvent event){
		File file = new File(filePath);
		return getModules(file,moduleType,event);
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.lang.String, int)
	 */
	public List<Module> getModules(String filePath,int moduleType){	
		return getModules(filePath,moduleType,null);
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.lang.String)
	 */
	public List<Module> getModules(String filePath){
		return getModules(filePath,MODULE_TYPE_NORMAL);
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModule(java.lang.String, int)
	 */
	public Module getModule(String filePath,int moduleType){
		List<Module> modules =  getModules(filePath,moduleType);
		if(modules!=null && modules.size()>0){
			return modules.get(0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModule(java.lang.String)
	 */
	public Module getModule(String filePath){
		return getModule(filePath,MODULE_TYPE_NORMAL);
	}


	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.io.File)
	 */
	public List<Module> getModules(File file){
		return getModules(file,MODULE_TYPE_NORMAL);
	}

	/**
	 * 
	 */
	public List<Module> getModules(File file,int moduleType){
		return this.getModules(file,moduleType,null);
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.io.File, int)
	 */
	public abstract List<Module> getModules(File file,int moduleType,ParserEvent event);

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#dispose()
	 */
	public void dispose(){
		this.isDispose = true;
		this.threadPool = null;
		this.aliasList = null;
	}

	public boolean isDisposed(){
		return isDispose;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getMdType()
	 */
	public int getMdType(){
		return mdType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setMdType(int)
	 */
	public void setMdType(int mdType){
		this.mdType = mdType;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getCharset()
	 */
	public String getCharset() {
		return charset;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setCharset(java.lang.String)
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getFilter()
	 */
	public FileFilter getFilter() {
		return filter;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setFilter(java.io.FileFilter)
	 */
	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}


	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getThreadPool()
	 */
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setThreadPool(java.util.concurrent.ExecutorService)
	 */
	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public List<AliasInfo> getAliasList() {
		return aliasList;
	}

	public void setAliasList(List<AliasInfo> aliasList) {
		this.aliasList = aliasList;
	}

	public NameConverter getNameConverter() {
		return nameConverter;
	}

	public void setNameConverter(NameConverter nameConverter) {
		this.nameConverter = nameConverter;
	}

	public boolean isNodeJs(){
		return isNodeJs;
	}

	public void setIsNodeJs(boolean isNodeJs){
		this.isNodeJs = isNodeJs;
	}

	public String getDefineKeyWord() {
		return defineKeyWord;
	}

	public String getRequireKeyWord() {
		return requireKeyWord;
	}

	public void setDefineKeyWord(String defineKeyWord){
		if(defineKeyWord!=null && defineKeyWord.length()>0){
			this.defineKeyWord = defineKeyWord;
		}
	}

	public void setRequireKeyWord(String requireKeyWord){
		if(requireKeyWord!=null && requireKeyWord.length()>0){
			this.requireKeyWord = requireKeyWord;
		}
	}
	
	public boolean isRemoveDuplicateRequire() {
		return removeDuplicateRequire;
	}
	public void setIsRemoveDuplicateRequire(boolean removeDuplicateRequire) {
		this.removeDuplicateRequire = removeDuplicateRequire;
	}

}
