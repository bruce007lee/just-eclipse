package com.alibaba.just.api.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.exception.ModuleParseException;

/**
 * 
 * @author bruce.liz
 *
 */
public interface ModuleParser {

	public static final String DEFINE_KEY_REG = "define";
	public static final String DEFAULT_CHARSET = "GBK";
	public static final int MODULE_TYPE_NORMAL = 0;
	public static final int MODULE_TYPE_ANONYMOUS = 1;
	public static final int MODULE_TYPE_ALL = 2;
	/**
	 * 默认的js文件filter
	 */
	public static final FileFilter DEFAULT_FILE_FILTER = new FileFilter() {
		public boolean accept(File file) {
			if (file.isFile()) {
				String name = file.getName().toLowerCase();
				if (name.endsWith(".js") && !name.endsWith("-min.js")) {
					return true;
				} else {
					return false;
				}
			} else if (file.isDirectory() && file.isHidden()) {
				return false;
			}
			return true;
		}
	};

	/**
	 * 递归得到指定模块的所有依赖子模块
	 * @param module
	 * @param list
	 * @return
	 * @throws ModuleParseException 
	 */
	public List<Module> getAllRequiredModules(Module module,
			List<Module> list) throws ModuleParseException;

	/**
	 * 得到引用指定模块的父模块
	 * @param module
	 * @param list
	 * @return
	 */
	public List<Module> getUsedModules(Module module, List<Module> list);

	public List<Module> getAllModules(String path);

	/**
	 * 获取指定路径下的所有指定类型的模块
	 * @param path
	 * @param moduleType
	 * @return
	 */
	public List<Module> getAllModules(String path, int moduleType);

	/**
	 * 获取指定路径下的所有指定类型的模块
	 * @param paths
	 * @param moduleType
	 * @param event
	 * @return
	 */
	public List<Module> getAllModules(List<String> paths,final int moduleType,ParserEvent event);

	/**
	 * 获取指定路径下的所有指定类型的模块
	 * @param paths
	 * @param moduleType
	 * @return
	 */
	public List<Module> getAllModules(List<String> paths,final int moduleType);

	/**
	 * 得到当前模块文件的所有模块
	 * @param filePath
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(String filePath, int moduleType);

	/**
	 * 得到当前模块文件的所有非匿名模块
	 * @param filePath
	 * @return
	 */
	public List<Module> getModules(String filePath);

	/**
	 * 得到当前模块文件的默认模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return 
	 */
	public Module getModule(String filePath, int moduleType);

	/**
	 * 得到当前模块文件的默认非匿名模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return
	 */
	public Module getModule(String filePath);

	/**
	 * 默认取非匿名模块
	 * @param file
	 * @return
	 */
	public List<Module> getModules(File file);

	/**
	 * 由文件获取指定类型的模块
	 * @param file
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(File file, int moduleType);

	/**
	 * 释放资源
	 */
	public void dispose();
	
	public boolean isDisposed();
	
	public String getCharset();

	public void setCharset(String charset);

	public FileFilter getFilter();

	public void setFilter(FileFilter filter);

	public List<AliasInfo> getAliasList();

	public void setAliasList(List<AliasInfo> aliasList);

	public ExecutorService getThreadPool();

	public void setThreadPool(ExecutorService threadPool);
	
	public void setDefineKeyWord(String str);

}