package com.alibaba.just.api.parser;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.StringLiteral;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.exception.ModuleParseException;
import com.alibaba.just.util.FileUtil;


/**
 * 
 * @author bruce.liz
 */
public class ModuleParser {
	private ExecutorService threadPool = null;

	public static final String DEFAULT_CHARSET = "GBK";	
	private String charset = DEFAULT_CHARSET;
	private static final String DEFINE_KEY_REG = "^(\\w+\\.)*define$";

	public static final int MODULE_TYPE_NORMAL = 0;
	public static final int MODULE_TYPE_ANONYMOUS = 1;
	public static final int MODULE_TYPE_ALL = 2;

	private boolean isDispose = false;

	/**
	 * 默认的js文件filter
	 */
	public static final FileFilter DEFAULT_FILE_FILTER = new FileFilter(){
		public boolean accept(File file) {
			if(file.isFile()){
				String name = file.getName().toLowerCase();
				if(name.endsWith(".js") && !name.endsWith("-min.js")){
					return true;
				}else{
					return false;
				}
			}else if(file.isDirectory() && file.isHidden() ){
				return false;
			}
			return true;
		}			
	};

	protected FileFilter filter = DEFAULT_FILE_FILTER; 

	public ModuleParser(String charset){
		this.charset = charset;		
	}

	public ModuleParser(){}

	/**
	 * 处理folder中文件
	 * @param file
	 * @param list
	 * @param moduleType
	 * @return
	 */
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
	}

	private List<Module> processFolder(File file,final List<Module> list,final int moduleType,List<Callable<Object>> tasks){
		if(isDispose && !file.exists() || !file.isDirectory()){return list;}
		File[]  flist  = file.listFiles(filter);		
		if(threadPool!=null){
			/*当使用线程池时*/
			for(final File f:flist){
				if(f.isFile()){
					tasks.add(new Callable<Object>(){
						public Object call() throws Exception {
							list.addAll(getModules(f,moduleType));
							return null;
						}					
					});
				}else if(f.isDirectory()){
					processFolder(f,list,moduleType,tasks);
				}
			}
		}else{
			/*当不使用线程池时*/
			for(File f:flist){
				if(f.isFile()){

					list.addAll(getModules(f,moduleType));

				}else if(f.isDirectory()){
					processFolder(f,list,moduleType);
				}
			}

		}
		return list;
	}

	/**
	 * 模块是否在列表中重复
	 * @param module
	 * @param list
	 * @return
	 */
	private boolean isDuplicate(Module module,List<Module> list){	
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
	private List<Module> getAllRequiredModules(Module module,List<Module> moduleTree,List<Module> newList,List<Module> list) throws ModuleParseException{
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

	/**
	 * 递归得到指定模块的所有依赖子模块
	 * @param module
	 * @param list
	 * @return
	 * @throws ModuleParseException 
	 */
	public List<Module> getAllRequiredModules(Module module,List<Module> list) throws ModuleParseException{
		return getAllRequiredModules(module,new ArrayList<Module>(), new ArrayList<Module>() ,list);
	}


	/**
	 * 得到引用指定模块的父模块
	 * @param module
	 * @param list
	 * @return
	 * @throws ModuleParseException
	 */
	public List<Module> getUsedModules(Module module,List<Module> list) throws ModuleParseException{
		List<String> subModNames = null;
		List<Module> rsList = new ArrayList<Module>();
		for(Module m : list){
			subModNames = m.getRequiredModuleNames();
			for(String subName : subModNames){
				if(module.getName() != null && module.getName().equals(subName)){
					rsList.add(m);
					break;
				}
			}
		}
		return rsList;
	}

	/**
	 * 
	 * @param moduleName
	 * @param list
	 * @return
	 */
	private Module getModuleByName(String moduleName,List<Module> list){

		if(moduleName==null){
			return null;
		}
		for(Module submod : list){
			if(moduleName.equals(submod.getName())){
				return submod;
			}
		}

		return null;
	}

	public List<Module> getAllModules(String path){
		return getAllModules(path,MODULE_TYPE_NORMAL);
	}

	/**
	 * 获取指定路径下的所有指定类型的模块
	 * @param path
	 * @param moduleType
	 * @return
	 */
	public List<Module> getAllModules(String path,int moduleType){
		List<Module> moduleList = new Vector<Module>();

		File  file = new File(path);

		if(!isDispose){
			if(file.exists()){
				if(file.isDirectory()){
					processFolder(file, moduleList,moduleType);
				}else if(file.isFile()){
					moduleList.addAll(getModules(file,moduleType));
				}
			}
		}

		return moduleList;
	}


	/**
	 * 得到当前模块文件的所有模块
	 * @param filePath
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(String filePath,int moduleType){
		File file = new File(filePath);
		return getModules(file,moduleType);
	}

	/**
	 * 得到当前模块文件的所有非匿名模块
	 * @param filePath
	 * @return
	 */
	public List<Module> getModules(String filePath){
		return getModules(filePath,MODULE_TYPE_NORMAL);
	}

	/**
	 * 得到当前模块文件的默认模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return
	 */
	public Module getModule(String filePath,int moduleType){
		List<Module> modules =  getModules(filePath,moduleType);
		if(modules!=null && modules.size()>0){
			return modules.get(0);
		}
		return null;
	}

	/**
	 * 得到当前模块文件的默认非匿名模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return
	 */
	public Module getModule(String filePath){
		return getModule(filePath,MODULE_TYPE_NORMAL);
	}

	/**
	 * 默认取非匿名模块
	 * @param file
	 * @return
	 */
	public List<Module> getModules(File file){
		return getModules(file,MODULE_TYPE_NORMAL);
	}

	/**
	 * 由文件获取指定类型的模块
	 * @param file
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(File file,int moduleType){

		List<Module> moduleList = new ArrayList<Module>();

		String absPath = file.getAbsolutePath();
		if(!isDispose && file.exists() && (filter==null || (filter!=null && filter.accept(file)))){
			String content = null;
			try {
				content = FileUtil.getFileContent(file, charset);
			} catch (Exception e) {}

			if(content==null){return null ;}


			/*CompilerEnvirons ce = new CompilerEnvirons();
			ce.setRecordingLocalJsDocComments(true);
			ce.setRecordingComments(true);
			Parser  parser = new  Parser(ce);
			 */
			Parser  parser = new Parser();
			AstRoot astRoot = parser.parse(content, null, 0);
			Node node = astRoot.getFirstChild();
			while(node!=null){
				//System.out.println(node.getClass().getName());
				if(ExpressionStatement.class.isInstance(node)){
					ExpressionStatement es = (ExpressionStatement)node;
					AstNode astNode = es.getExpression();
					if(FunctionCall.class.isInstance(astNode)){
						FunctionCall fc = (FunctionCall)astNode;
						AstNode nameNode = fc.getTarget();
						//System.out.println("NAME:"+n.getIdentifier());	
						if(Pattern.matches(DEFINE_KEY_REG,nameNode.toSource().trim())){

							List<AstNode> args = fc.getArguments();
							//校验普通模块
							if(args.size()==3 && (moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL)){
								AstNode stringNode = args.get(0);
								AstNode arrayNode = args.get(1);
								AstNode funNode = args.get(2);

								if(StringLiteral.class.isInstance(stringNode) &&
										ArrayLiteral.class.isInstance(arrayNode) &&
										FunctionNode.class.isInstance(funNode)){
									Module module = new Module();
									module.setAnonymous(false);
									module.setName(((StringLiteral)stringNode).getValue(false));
									module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)arrayNode));
									module.setFilePath(absPath);
									moduleList.add(module);
								}
							}

							//校验匿名模块
							if(args.size()==2 && (moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS)){
								AstNode arrayNode = args.get(0);
								AstNode funNode = args.get(1);
								if(ArrayLiteral.class.isInstance(arrayNode) &&
										FunctionNode.class.isInstance(funNode)){
									Module module = new Module();
									module.setAnonymous(true);
									module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)arrayNode));
									module.setFilePath(absPath);
									moduleList.add(module);
								}
							}

						}

					}
				}
				node = node.getNext();
			}
		}	
		return moduleList;
	}

	/**
	 * 
	 * @param module
	 * @return
	 */
	private List<String> getRequiredModules(ArrayLiteral array){
		List<String> list = new ArrayList<String>();
		List<AstNode> strList = array.getElements();		
		for(AstNode strNode:strList){
			if(StringLiteral.class.isInstance(strNode)){
				list.add(((StringLiteral)strNode).getValue(false));
			}
		}
		return list;
	}

	/**
	 * 释放资源
	 */
	public void dispose(){
		this.isDispose = true;
		this.threadPool = null;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public FileFilter getFilter() {
		return filter;
	}

	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}


	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

}
